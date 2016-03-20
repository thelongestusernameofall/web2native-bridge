/*
 *  Copyright 2006-2016 WebPKI.org (http://webpki.org).
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
package org.webpki.tools.svg.test;

import java.io.IOException;

import org.webpki.tools.svg.SVGAddDouble;
import org.webpki.tools.svg.SVGAddOffset;
import org.webpki.tools.svg.SVGAnchor;
import org.webpki.tools.svg.SVGCenter;
import org.webpki.tools.svg.SVGCircle;
import org.webpki.tools.svg.SVGDocument;
import org.webpki.tools.svg.SVGDoubleValue;
import org.webpki.tools.svg.SVGEllipse;
import org.webpki.tools.svg.SVGEmbeddedText;
import org.webpki.tools.svg.SVGHorizontalLine;
import org.webpki.tools.svg.SVGObject;
import org.webpki.tools.svg.SVGPath;
import org.webpki.tools.svg.SVGPathValues;
import org.webpki.tools.svg.SVGPolygon;
import org.webpki.tools.svg.SVGRect;
import org.webpki.tools.svg.SVGShaderTemplate;
import org.webpki.tools.svg.SVGText;
import org.webpki.tools.svg.SVGValue;
import org.webpki.tools.svg.SVGVerticalLine;

public class Test extends SVGDocument {
    public Test() {
        super(15, 10);
    }

    SVGDoubleValue linesLength = new SVGDoubleValue(500);
    double verticalLineWidth = 2;
    String verticalLineColor = "#4a73bb";
    SVGVerticalLine vertLine1;
    SVGVerticalLine vertLine2;
    
    SVGValue lines1_X;
    SVGValue lines2_X;
    SVGValue linesY;
    SVGValue boxOneX;
    SVGValue boxOneY;
    SVGValue lastY;
    SVGValue boxOneWidth;
    SVGValue boxOneHeight;

    static final int POINT_INDICATOR_FONT_SIZE        = 10;
    static final double POINT_INDICATOR_BORDER__WIDTH = 0.5;
    static final double POINT_INDICATOR_DIAMETER      = 11;
    
    void createPointIndicator(SVGValue x,
                              SVGValue y,
                              int q,
                              String optionalLink,
                              String optionalToolTip) {
        SVGCircle circ = new SVGCircle(x, y,
                                       new SVGDoubleValue(POINT_INDICATOR_DIAMETER),
                                       POINT_INDICATOR_BORDER__WIDTH,
                                       "#000000",
                                       "#ffffff");
        if (optionalLink != null) {
            circ.setLink(optionalLink, optionalToolTip, false);
        }
        add(circ);
        circ.addAfterObject(new SVGText(new SVGAddOffset(x,  POINT_INDICATOR_DIAMETER / 2),
                                        new SVGAddOffset(y, POINT_INDICATOR_DIAMETER / 2),
                                        "Sans-serif",
                                        POINT_INDICATOR_FONT_SIZE,
                                        SVGText.TEXT_ANCHOR.MIDDLE,
                                        Integer.toString(q)).setDy("0.35em"));
    }
        
    @Override
    public void generate() {
        add(vertLine1 = new SVGVerticalLine(lines1_X = new SVGDoubleValue(50), 
                                linesY = new SVGDoubleValue(30),
                                linesLength,
                                verticalLineWidth,
                                verticalLineColor));

        add(new SVGRect(boxOneX = new SVGAddOffset(lines1_X, 20),
                        boxOneY = new SVGDoubleValue(0),
                        boxOneWidth = new SVGDoubleValue(80),
                        boxOneHeight = new SVGDoubleValue(20),
                        2.5,
                        "#FF0000",
                        null).setRadiusX(3).setRadiusY(3));

        add(vertLine2 = new SVGVerticalLine(lines2_X = new SVGAddDouble(boxOneX, boxOneWidth, 20), 
                                linesY,
                                linesLength,
                                verticalLineWidth,
                                verticalLineColor));
        
        add(new SVGRect(        new SVGCenter(lines1_X, lines2_X, 10),
                                new SVGAddDouble(boxOneY, boxOneHeight, 20),
                                new SVGDoubleValue(10),
                                new SVGDoubleValue(20),
                                null,
                                null,
                                "#00FF00"));
        
        add(new SVGText(new SVGCenter(lines1_X, lines2_X),
                        lastY = new SVGDoubleValue(230),
                        "Sans-serif",
                        10,
                        SVGText.TEXT_ANCHOR.MIDDLE,
                        "Hi There!\nOtherline, yes\n\nempty").setFontColor("#0000FF"));

        add(new SVGHorizontalLine(vertLine1, 
                                  vertLine2,
                                  new SVGDoubleValue(90),
                                  0.8,
                                  "#000000").setLeftArrow(new SVGHorizontalLine.Arrow(4, 3, 0.5)));

        add(new SVGHorizontalLine(vertLine1, 
                vertLine2,
                new SVGDoubleValue(92),
                0.5,
                "#000000").setRightArrow(new SVGHorizontalLine.Arrow(4, 3, 0.5)).setDashMode(2.5, 1.8));

        add(new SVGHorizontalLine(vertLine1, 
                vertLine2,
                new SVGDoubleValue(95),
                0.8,
                "#000000").setLeftGutter(2));

        
        add(new SVGPolygon(        new SVGCenter(lines1_X, lines2_X, 10),
                new SVGDoubleValue(60),
                new double[]{0, 5,
                             10, 0,
                             10, 10},
                null,
                null,
                "#ff0000"));

        add(new SVGHorizontalLine(vertLine1, 
                                  vertLine2,
                                  new SVGDoubleValue(140),
                                  0.8,
                                  "#000000")
                    .setLeftArrow(new SVGHorizontalLine.Arrow(4, 3, 0.5))
                    .setRect(new SVGHorizontalLine.Rect(null,
                                                        null,
                                                        new SVGDoubleValue(44),
                                                        new SVGDoubleValue(16),
                                                        null,
                                                        null,
                                                        "#0c960c").setRadiusX(4).setRadiusY(4))
                    .setText(new SVGHorizontalLine.Text(null,
                                                        null,
                                                        "Sans-serif",
                                                        12,
                                                         "W2NB").setFontColor("#ffffff")));

        add(new SVGHorizontalLine(vertLine1, 
                                  vertLine2,
                                  new SVGDoubleValue(170),
                                  0.8,
                                  "#000000")
                    .setLeftArrow(new SVGHorizontalLine.Arrow(4, 3, 0.5))
                    .setRect(new SVGHorizontalLine.Rect(null,
                                      null,
                                      new SVGDoubleValue(44),
                                      new SVGDoubleValue(14),
                                      0.5,
                                      "#000000",
                                      "#FFFFFF"
  ).setRadiusX(4).setRadiusY(4))
  .setText(new SVGHorizontalLine.Text(null,
                                      null,
                                      "Sans-serif",
                                      12,
                                      "HTTP")));
        add(new SVGHorizontalLine(vertLine1, 
                vertLine2,
                new SVGDoubleValue(200),
                0.8,
                "#000000")
  .setLeftArrow(new SVGHorizontalLine.Arrow(4, 3, 0.5))
.setText(new SVGHorizontalLine.Text(10.0,
                    5.0,
                    "Sans-serif",
                    12,
                    "Some text")));
        
 createPointIndicator(new SVGDoubleValue(30),
         new SVGDoubleValue(195),
                1,
                null,
                null);

        
SVGObject someRect;        
add(someRect = new SVGRect(createDocumentAnchor(370, 150, SVGAnchor.ALIGNMENT.BOTTOM_RIGHT),
                        new SVGDoubleValue(156),
                        new SVGDoubleValue(48),
                        0.5,
                        "#969191",
                        "#edbca6").setRadiusX(10).setRadiusY(10));

add(new SVGRect(new SVGAnchor(someRect, someRect, SVGAnchor.ALIGNMENT.TOP_LEFT)
.derive(new SVGDoubleValue(2), new SVGDoubleValue(2), SVGAnchor.ALIGNMENT.TOP_LEFT),
        new SVGDoubleValue(152),
        new SVGDoubleValue(44),
        null,
        null,
        "#ffffff").setRadiusX(9)
                  .setRadiusY(9)
                  .setFilter("url(#actorsBlur)")
                  .setLink("http://google.com", "Search something", false));

SVGAnchor anchor = new SVGAnchor(someRect, someRect, SVGAnchor.ALIGNMENT.BOTTOM_CENTER);
add(new SVGRect(anchor.derive(new SVGDoubleValue(20), new SVGDoubleValue(10), SVGAnchor.ALIGNMENT.TOP_LEFT),
        new SVGDoubleValue(80),
        new SVGDoubleValue(20),
        2.5,
        "#00FF00",
        null).setRadiusX(3).setRadiusY(3));
add(new SVGRect(anchor.derive(new SVGDoubleValue(-19.35), new SVGDoubleValue(9.35), SVGAnchor.ALIGNMENT.TOP_RIGHT),
        new SVGDoubleValue(81.5),
        new SVGDoubleValue(60),
        1.2,
        "#FF0000",
        "#FFFFE8").setShader(new SVGShaderTemplate("url(#messageBlur)", "#7f7f7f", 2, 2)));

SVGObject lo= add(new SVGCircle(new SVGDoubleValue(300), new SVGDoubleValue(250),
        new SVGDoubleValue(50),
        1.2,
        "#FF0000",
        "#FFFFE8").setShader(new SVGShaderTemplate("url(#messageBlur)", "#7f7f7f", 2.5, 2.5)));

    add(new SVGPath(new SVGDoubleValue(400), new SVGDoubleValue(220),
        new SVGPathValues().moveAbsolute(0, 0)
        .lineToRelative(100, 0)
        .cubicBezierRelative(25, 0, 25, 0, 25,-25)
        .lineToRelative(0, -100)
        .cubicBezierRelative(0, -25, 0, -25, 25,-25)
        .lineToRelative(50, 0)
        .cubicBezierRelative(25, 0, 25, 0, 25, 25)
        .lineToRelative(0, 150)
        .cubicBezierRelative(0, 25, 0, 25, -25, 25)
        .lineToRelative(-200, 0),
        1.2,
        "#000000",
        null).setDashMode(1.2, 1));


add(new SVGEllipse(anchor.derive(new SVGDoubleValue(20), new SVGDoubleValue(9.35), SVGAnchor.ALIGNMENT.TOP_LEFT),
        new SVGDoubleValue(70),
new SVGDoubleValue(50),
        1.2,
        "#FF0000",
        "#FFFFE8").setShader(new SVGShaderTemplate("url(#messageBlur)", "#7f7f7f", 3, 3)).setDashMode(3, 2));

add(new SVGRect(createDocumentAnchor(600, 150, SVGAnchor.ALIGNMENT.BOTTOM_LEFT),
        new SVGDoubleValue(40),
        new SVGDoubleValue(40),
        1.2,
        "#FF0000",
        "#FFFFE8").setShader(new SVGShaderTemplate("url(#messageBlur)", "#7f7f7f", 3, 3))
        .addCenterText(0, "Sans-serif", 10, "CTR"));

add(new SVGRect(createDocumentAnchor(600, 250, SVGAnchor.ALIGNMENT.TOP_LEFT),
        new SVGDoubleValue(200),
        new SVGDoubleValue(400),
        1.2,
        "#FF0000",
        "#FFFFE8").setShader(new SVGShaderTemplate("url(#messageBlur)", "#7f7f7f", 3, 3))
        .addLeftText(5,
                     10,
                     "Sans-serif",
                     10,
                     "Hi, this is a few\nlines of text\nto be showed in a square"));

SVGEmbeddedText et = new SVGEmbeddedText(org.webpki.w2nb.webpayment.resources.svg.diagrams.SupercardGlyphs.class);

String superCardString = "SuperCard";

double x = 100;

for (char c : superCardString.toCharArray()) {
    
    SVGEmbeddedText.DecodedGlyph dg = et.getDecodedGlyph(c, 50);

add(new SVGPath(new SVGDoubleValue(x), new SVGDoubleValue(680),
        dg.getSVGPathValues(),
        null,
        null,
        "#000000"));
x += dg.getXAdvance() + 3;
}


linesLength.setDouble(lo.getPrimaryY().getDouble() + lo.getPrimaryHeight().getDouble() - linesY.getDouble() + 15);
}
    
 }
