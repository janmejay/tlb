package com.thoughtworks.cruise.tlb.utils;

import org.junit.Test;
import static org.junit.Assert.assertThat;
import org.dom4j.Element;
import org.dom4j.DocumentFactory;
import static org.hamcrest.core.Is.is;

import java.util.List;
import java.util.HashMap;

import com.thoughtworks.cruise.tlb.TestUtil;

public class XmlUtilTest {
    @Test
    public void shouldUnderstandsLoadingStringAsXML() throws Exception{
        Element element = XmlUtil.domFor("" + //to fool intelliJ and keep it from formating it idiotically
                "<foo>" +
                "  <bar>baz</bar>" +
                "  <baz>" +
                "    <quux>bang</quux>" +
                "  </baz>" +
                "</foo>");
        List barTexts = element.selectNodes("//bar/.");
        assertThat(barTexts.size(), is(1));
        assertThat(((Element) barTexts.get(0)).getText(), is("baz"));
    }

    @Test
    public void shouldUnderstandAtomFeedStringAsXML() throws Exception{
        String stageFeedPage = TestUtil.fileContents("resources/stages_p1.xml");
        Element element = XmlUtil.domFor(stageFeedPage);
        List entryIds = element.selectNodes("//a:entry/a:id");
        assertThat(entryIds.size(), is(3));
        assertThat(((Element) entryIds.get(0)).getText(), is("72"));
        assertThat(((Element) entryIds.get(1)).getText(), is("66"));
        assertThat(((Element) entryIds.get(2)).getText(), is("60"));
    }
    
    @Test
    public void shouldNotGetMessedUpWhenXmlNamespaceURL() throws Exception{
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("a", "http://foo.com/bar/baz");
        DocumentFactory.getInstance().setXPathNamespaceURIs(map);

        String stageFeedPage = com.thoughtworks.cruise.tlb.TestUtil.fileContents("resources/stages_p1.xml");
        Element element = XmlUtil.domFor(stageFeedPage);
        List entryIds = element.selectNodes("//a:entry/a:id");
        assertThat(entryIds.size(), is(3));
        assertThat(((Element) entryIds.get(0)).getText(), is("72"));
    }
}
