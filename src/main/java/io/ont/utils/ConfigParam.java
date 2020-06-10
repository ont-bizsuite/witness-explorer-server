package io.ont.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;


@Service("ConfigParam")
public class ConfigParam {


    /**
     * SDK params
     */
    @Value("${service.restfulUrl}")
    public String RESTFUL_URL;

    @Value("${middleware.url}")
    public String MIDDLEWARE_URL;

    @Value("${addon.id}")
    public String ADDON_ID;

    @Value("${tenant.id}")
    public String TENANT_ID;

    @Value("${contract.address}")
    public String CONTRACT_ADDRESS;

    @Value("${public.key}")
    public String PUBLIC_KEY;
}