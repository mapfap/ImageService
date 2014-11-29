package com.mapfap.image.entity.atom;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "feed")
@XmlAccessorType(XmlAccessType.FIELD)
public class Feed {

	public static String ATOM_XMLNS = "http://www.w3.org/2005/Atom";
	
	@XmlElement(name = "entry")
	public List<Entry> entries;
	
	@XmlAttribute(name = "xmlns")
	private String xmlns = ATOM_XMLNS;

	public List<Entry> getEntries() {
		return entries;
	}

	public void setEntries(List<Entry> entries) {
		this.entries = entries;
	}

	public String getXmlns() {
		return xmlns;
	}

	public void setXmlns(String xmlns) {
		this.xmlns = xmlns;
	}

}
