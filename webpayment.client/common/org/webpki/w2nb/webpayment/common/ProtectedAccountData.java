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
package org.webpki.w2nb.webpayment.common;

import java.io.IOException;
import java.util.GregorianCalendar;

import org.webpki.json.JSONObjectReader;
import org.webpki.json.JSONObjectWriter;

public class ProtectedAccountData implements BaseProperties {
    
    public static JSONObjectWriter encode(String accountId,
                                          String accountHolder,
                                          GregorianCalendar expires,
                                          String accountSecurityCode) throws IOException {
        return new JSONObjectWriter()
            .setString(ACCOUNT_ID_JSON, accountId)
            .setString(ACCOUNT_HOLDER_JSON, accountHolder)
            .setDateTime(EXPIRES_JSON, expires.getTime(), true)
            .setString(ACCOUNT_SECURITY_CODE_JSON, accountSecurityCode);
    }
    
    JSONObjectReader root;

    public ProtectedAccountData(JSONObjectReader rd) throws IOException {
        root = rd;
        accountId = rd.getString(ACCOUNT_ID_JSON);
        rd.getString(ACCOUNT_HOLDER_JSON);
        rd.getDateTime(EXPIRES_JSON);
        rd.getString(ACCOUNT_SECURITY_CODE_JSON);
        rd.checkForUnread();
    }

    String accountId;
    public String getAccountId() {
        return accountId;
    }
    
    @Override
    public String toString() {
        return root.toString();
    }
}
