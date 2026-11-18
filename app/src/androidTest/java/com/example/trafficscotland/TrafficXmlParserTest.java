package com.example.trafficscotland;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.trafficscotland.model.Situation;
import com.example.trafficscotland.parser.TrafficXmlParser;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class TrafficXmlParserTest {

    @Test
    public void testParseXmlWithInvalidTagContainingDot() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<d2LogicalModel xmlns=\"http://datex2.eu/schema/2/2_0\">" +
                "<payloadPublication xsi:type=\"NonGeneralPublication\">" +
                "<situation id=\"sit1\">" +
                "<situationRecord id=\"rec1\" xsi:type=\"TrafficObstruction\">" +
                "<generalPublicComment>" +
                "<value>Road test with <c.length> item</value>" +
                "</generalPublicComment>" +
                "<overallStartTime>2023-10-01T10:00:00Z</overallStartTime>" +
                "<roadNumber>A9</roadNumber>" +
                "<latitude>56.4907</latitude>" +
                "<longitude>-4.2026</longitude>" +
                "</situationRecord>" +
                "</situation>" +
                "</payloadPublication>" +
                "</d2LogicalModel>";

        TrafficXmlParser parser = new TrafficXmlParser();
        ArrayList<Situation> situations = parser.parse(xml, "Incident");

        assertNotNull(situations);
        assertEquals(1, situations.size());
        assertEquals("rec1", situations.get(0).getId());
    }
}
