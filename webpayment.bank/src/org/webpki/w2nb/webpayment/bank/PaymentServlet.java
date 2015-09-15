/*
 *  Copyright 2006-2015 WebPKI.org (http://webpki.org).
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.webpki.w2nb.webpayment.bank;

import java.io.IOException;

import java.math.BigDecimal;

import java.net.URL;

import java.security.GeneralSecurityException;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.webpki.json.JSONDecoderCache;
import org.webpki.json.JSONObjectReader;
import org.webpki.json.JSONObjectWriter;
import org.webpki.json.JSONOutputFormats;
import org.webpki.json.JSONParser;

import org.webpki.net.HTTPSWrapper;

import org.webpki.util.ISODateTime;

import org.webpki.w2nb.webpayment.common.AccountTypes;
import org.webpki.w2nb.webpayment.common.Authority;
import org.webpki.w2nb.webpayment.common.BaseProperties;
import org.webpki.w2nb.webpayment.common.EncryptedData;
import org.webpki.w2nb.webpayment.common.ErrorReturn;
import org.webpki.w2nb.webpayment.common.FinalizeRequest;
import org.webpki.w2nb.webpayment.common.FinalizeResponse;
import org.webpki.w2nb.webpayment.common.PayeeAccountDescriptor;
import org.webpki.w2nb.webpayment.common.ReserveOrDebitRequest;
import org.webpki.w2nb.webpayment.common.AuthorizationData;
import org.webpki.w2nb.webpayment.common.ReserveOrDebitResponse;
import org.webpki.w2nb.webpayment.common.Messages;
import org.webpki.w2nb.webpayment.common.PaymentRequest;
import org.webpki.w2nb.webpayment.common.ProtectedAccountData;

import org.webpki.webutil.ServletUtil;

public class PaymentServlet extends HttpServlet implements BaseProperties {

    private static final long serialVersionUID = 1L;
    
    static Logger logger = Logger.getLogger(PaymentServlet.class.getCanonicalName());
    
    static final int TIMEOUT_FOR_REQUEST = 5000;

    static int referenceId = 164006;

    static String portFilter(String url) throws IOException {
        // Our JBoss installation has some port mapping issues...
        if (BankService.serverPortMapping == null) {
            return url;
        }
        URL url2 = new URL(url);
        return new URL(url2.getProtocol(),
                       url2.getHost(),
                       BankService.serverPortMapping,
                       url2.getFile()).toExternalForm(); 
    }

    private JSONObjectWriter processReserveOrDebitRequest(JSONObjectReader payeeRequest)
    throws IOException, GeneralSecurityException {
        // Read the attested and encrypted request
       ReserveOrDebitRequest attestedEncryptedRequest = new ReserveOrDebitRequest(payeeRequest);

       // Decrypt the encrypted request
       AuthorizationData authorizationData =
               attestedEncryptedRequest.getDecryptedAuthorizationRequest(BankService.decryptionKeys);

       // In the indirect mode the merchant is the only one who can provide the client's IP address
       String clientIpAddress = attestedEncryptedRequest.getClientIpAddress();

       // Client IP could be used for risk-based authentication, here it is only logged
       logger.info("Client address: " + clientIpAddress);

       // Verify that the outer signature (the user's) matches the bank's client root
       authorizationData.getSignatureDecoder().verify(BankService.clientRoot);

       // Get the embedded (counter-signed) payment request
       PaymentRequest paymentRequest = authorizationData.getPaymentRequest();

       // Verify that the merchant's signature belongs to a valid merchant trust network
       paymentRequest.getSignatureDecoder().verify(BankService.merchantRoot);
       
       ////////////////////////////////////////////////////////////////////////////
       // We got an authentic request.  Now we need to check available funds etc.//
       // Since we don't have a real bank this part is rather simplistic :-)     //
       ////////////////////////////////////////////////////////////////////////////

       // Sorry but you don't appear to have a million bucks :-)
       if (paymentRequest.getAmount().compareTo(new BigDecimal("1000000.00")) >= 0) {
           return ReserveOrDebitResponse.encode(attestedEncryptedRequest.isDirectDebit(),
                                                new ErrorReturn(ErrorReturn.ERRORS.INSUFFICIENT_FUNDS));
       }

       // Separate credit-card and account2account payments
       PayeeAccountDescriptor payeeAccount = null;
       JSONObjectWriter encryptedCardData = null;
       if (AccountTypes.fromType(authorizationData.getAccountType()).isAcquirerBased()) {
           logger.info("card");
           String authorityUrl = attestedEncryptedRequest.getAcquirerAuthorityUrl();
           HTTPSWrapper wrap = new HTTPSWrapper();
           wrap.setTimeout(TIMEOUT_FOR_REQUEST);
           wrap.setHeader("Content-Type", JSON_CONTENT_TYPE);
           wrap.setRequireSuccess(true);
           wrap.makeGetRequest(portFilter(authorityUrl));
           if (!wrap.getContentType().equals(JSON_CONTENT_TYPE)) {
               throw new IOException("Content-Type must be \"" + JSON_CONTENT_TYPE + "\" , found: " + wrap.getContentType());
           }
           Authority authority = new Authority(JSONParser.parse(wrap.getData()),authorityUrl);
           JSONObjectWriter protectedAccountData =
                ProtectedAccountData.encode(authorizationData.getAccountId(),
                                            "Luke Skywalker",
                                            ISODateTime.parseDateTime("2019-12-01T00:00:00Z"),
                                            "943");
           encryptedCardData = EncryptedData.encode(protectedAccountData,
                                                    authority.getDataEncryptionAlgorithm(),
                                                    authority.getPublicKey(),
                                                    authority.getKeyEncryptionAlgorithm());
        } else {
            logger.info("account");
            payeeAccount = attestedEncryptedRequest.getPayeeAccountDescriptors()[0];
        }

       return ReserveOrDebitResponse.encode(attestedEncryptedRequest,
                                            paymentRequest,
                                            authorizationData.getAccountType(),
                                            authorizationData.getAccountId(),
                                            encryptedCardData,
                                            payeeAccount,
                                            "#" + (referenceId ++),
                                             BankService.bankKey);
    }

    JSONObjectWriter processFinalizeRequest(JSONObjectReader payeeRequest) throws IOException, GeneralSecurityException {

        // Decode the finalize request message which the one which lifts money
        FinalizeRequest payeeFinalizationRequest = new FinalizeRequest(payeeRequest);

        // Get the embedded authorization presumably made by ourselves :-)
        ReserveOrDebitResponse embeddedResponse = payeeFinalizationRequest.getEmbeddedResponse();

        // Verify that the provider's signature really belongs to us
        ReserveOrDebitRequest.compareCertificatePaths(embeddedResponse.getSignatureDecoder().getCertificatePath(),
                                                      BankService.bankCertificatePath);

        //////////////////////////////////////////////////////////////////////////////
        // Since we don't have a real bank we simply return success...              //
        //////////////////////////////////////////////////////////////////////////////
       return FinalizeResponse.encode(payeeFinalizationRequest,
                                      "#" + (referenceId++),
                                      BankService.bankKey);
    }
        
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        JSONObjectWriter providerResponse = null;
        try {
            String contentType = request.getContentType();
            if (!contentType.equals(JSON_CONTENT_TYPE)) {
                throw new IOException("Content-Type must be \"" + JSON_CONTENT_TYPE + "\" , found: " + contentType);
            }
            JSONObjectReader payeeRequest = JSONParser.parse(ServletUtil.getData(request));
            logger.info("Received:\n" + payeeRequest);

            /////////////////////////////////////////////////////////////////////////////////////////
            // We rationalize here by using a single end-point for both reserve/debit and finalize //
            /////////////////////////////////////////////////////////////////////////////////////////
            providerResponse = 
                payeeRequest.getString(JSONDecoderCache.QUALIFIER_JSON).equals(Messages.FINALIZE_REQUEST.toString()) ?
                    processFinalizeRequest(payeeRequest) : processReserveOrDebitRequest(payeeRequest);

            logger.info("Returned to caller:\n" + providerResponse);
            
        } catch (Exception e) {
            providerResponse = Messages.createBaseMessage(Messages.ERROR_RESPONSE);
            providerResponse.setString(DESCRIPTION_JSON, e.getMessage());
            logger.log(Level.SEVERE, e.getMessage(), e);
        }

        response.setContentType(BankService.jsonMediaType);
        response.setHeader("Pragma", "No-Cache");
        response.setDateHeader("EXPIRES", 0);
        response.getOutputStream().write(providerResponse.serializeJSONObject(JSONOutputFormats.NORMALIZED));
      }
  }
