package io.ont.service;

import java.util.Map;

public interface SourcingService {

    Map<String, Object> getProof(String action, String hash) throws Exception;

    Map<String, Object> getBlock(String action, String hash) throws Exception;

}
