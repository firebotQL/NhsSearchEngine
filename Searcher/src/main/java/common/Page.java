package common;

import lombok.Data;

import javax.xml.bind.annotation.XmlRootElement;

@Data
@XmlRootElement
public class Page {
    private String url;
    private String title;
    private String content;
}

