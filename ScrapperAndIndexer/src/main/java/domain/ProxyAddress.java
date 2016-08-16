package domain;

import lombok.Value;

@Value
public class ProxyAddress {
    private String ip;
    private Integer port;
}
