package com.mapfap.image.entity;

import java.net.URL;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "link")
@XmlAccessorType(XmlAccessType.FIELD)
public class Link {
	
	private URL href;
	
	public Link() {
		
	}
	
	public Link(URL href) {
		this.href = href;
	}

	public URL getHref() {
		return href;
	}

	public void setHref(URL href) {
		this.href = href;
	}
	
}
