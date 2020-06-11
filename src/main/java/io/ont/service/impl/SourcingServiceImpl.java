package io.ont.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.ontio.account.Account;
import com.github.ontio.common.Helper;
import com.github.ontio.common.UInt256;
import com.github.ontio.core.payload.InvokeWasmCode;
import com.github.ontio.crypto.SignatureScheme;
import com.github.ontio.io.BinaryReader;
import com.github.ontio.merkle.MerkleVerifier;
import io.ont.controller.vo.Proof;
import io.ont.service.SourcingService;
import io.ont.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.util.*;


@Service
@Slf4j
public class SourcingServiceImpl implements SourcingService {

    @Autowired
    private SDKUtil sdkUtil;
    @Autowired
    private ConfigParam configParam;

    @Override
    public Map<String, Object> getProof(String action, String hash) throws Exception {
        Map<String, Object> verify = verifyProof(hash);
        return verify;
    }

    @Override
    public Map<String, Object> getBlock(String action, String hash) throws Exception {
        Map<String, Object> verify = verifyBlock(hash);
        return verify;
    }

    private Map<String, Object> verifyBlock(String hash) throws Exception {
        Proof proof = getProof(hash);
        return verifyBlock(proof, hash);
    }

    private Map<String, Object> verifyProof(String hash) throws Exception {
        Proof proof = getProof(hash);
        return verifyProof(proof, hash);
    }

    private Proof getProof(String hash) throws Exception {
        JSONObject result = (JSONObject) call("verify", new String[]{hash});
        if (null == result) {
            return null;
        }
        try {
            List<UInt256> proofs = new ArrayList<>();
            for (Object s : (JSONArray) result.get("proof")) {
                proofs.add(new UInt256(Helper.hexToBytes((String) s)));
            }
            UInt256[] ps = new UInt256[proofs.size()];
            proofs.toArray(ps);
            Proof proof = new Proof(new UInt256(Helper.hexToBytes((String) result.get("root"))),
                    (int) result.get("size"), (int) result.get("blockheight"), (int) result.get("index"), ps
            );
            return proof;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private Object call(String method, String[] hashes) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("pubKey", configParam.PUBLIC_KEY);
        params.put("hashes", hashes);
        return HelperUtil.rpcCall(method, params);
    }

    private Map<String, Object> verifyProof(Proof proof, String hash) throws Exception {
        if (null == proof) {
            Map<String, Object> map = new HashMap<>();
            map.put("root", "");
            map.put("index", 0);
            map.put("proof", new ArrayList<>());
            return map;
        }
        MerkleVerifier verifier = new MerkleVerifier();
        boolean result = verifier.VerifyLeafHashInclusion(new UInt256(Helper.hexToBytes(hash)), proof.index, proof.proof, proof.root, proof.size);
        if (result) {
            List<String> proofList = new ArrayList<>();
            for (UInt256 uInt256 : proof.proof) {
                proofList.add(uInt256.toHexString());
            }
            Map<String, Object> map = new HashMap<>();
            map.put("root", proof.root.toHexString());
            map.put("index", proof.index);
            map.put("proof", proofList);
            return map;
        }
        return null;
    }

    private Map<String, Object> verifyBlock(Proof proof, String hash) throws Exception {
        if (null == proof) {
            Map<String, Object> rootMap = new HashMap<>();
            rootMap.put("root", "");
            rootMap.put("size", 0);
            rootMap.put("blockHeight", 0);
            Map<String, Object> transactionMap = new HashMap<>();
            transactionMap.put("txHash", "");
            transactionMap.put("contractAddress", "");
            transactionMap.put("method", "");
            transactionMap.put("hashSize", 0);
            transactionMap.put("hashList", new ArrayList<>());
            Map<String, Object> map = new HashMap<>();
            map.put("rootMap",rootMap);
            map.put("transactionMap",transactionMap);
            return map;
        }
        MerkleVerifier verifier = new MerkleVerifier();
        boolean result = verifier.VerifyLeafHashInclusion(new UInt256(Helper.hexToBytes(hash)), proof.index, proof.proof, proof.root, proof.size);
        if (result) {
            Map<String, Object> rootMap = new HashMap<>();
            rootMap.put("root", proof.root.toHexString());
            rootMap.put("size", proof.size);
            rootMap.put("blockHeight", proof.blockheight);
            Map<String, Object> transactionMap = verifyBlock(proof);
            Map<String, Object> map = new HashMap<>();
            map.put("rootMap",rootMap);
            map.put("transactionMap",transactionMap);
            return map;
        }
        return null;
    }

    private Map<String, Object> verifyBlock(Proof proof) throws Exception {
        Map<String, Object> transactionMap = new HashMap<>();
        Object eventByHeight = sdkUtil.getEventByHeight(proof.blockheight);
        if (StringUtils.isEmpty(eventByHeight)) {
            return transactionMap;
        }
        for (Object o : (JSONArray) eventByHeight) {
            try {
                for (Object nObj : (JSONArray) ((Map) o).get("Notify")) {
                    Map m = (Map) nObj;
                    if (!configParam.CONTRACT_ADDRESS.equals(m.get("ContractAddress"))) continue;
                    JSONArray state = (JSONArray) m.get("States");
                    if (proof.root.toHexString().equals(state.getString(0)) && Integer.toString(proof.size).equals(state.getString(1))) {
                        String txHash = (String) ((Map) o).get("TxHash");
                        transactionMap = getHashes(txHash);
                        break;
                    }
                }
            } catch (Exception e) {
            }
        }
        return transactionMap;
    }


    public String batchAdd(String[] hashes) throws Exception {
        Object result = callAdd("batchAdd", hashes);
        try {
            return (String) result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object callAdd(String method, String[] hashes) throws Exception {
        Account account = new Account(Account.getPrivateKeyFromWIF(""), SignatureScheme.SHA256WITHECDSA);
        Map params = new HashMap();
        params.put("pubKey", Helper.toHexString(account.serializePublicKey()));
        params.put("hashes", hashes);
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : hashes) {
            stringBuilder.append(s);
            params.put("signature", Helper.toHexString(account.generateSignature(Helper.hexToBytes(stringBuilder.toString()), SignatureScheme.SHA256WITHECDSA, null)));
        }
        return HelperUtil.rpcCall(method, params);
    }

    public Map<String, Object> getHashes(String hash) throws Exception {
        InvokeWasmCode transaction = (InvokeWasmCode) sdkUtil.getTransaction(hash);
        BinaryReader reader = new BinaryReader(new ByteArrayInputStream(transaction.invokeCode));
        byte[] contractBytes = reader.readBytes(20);
        String contractStr = Helper.reverse(Helper.toHexString(contractBytes));
        System.out.println("contract address:" + contractStr);
        byte[] paramBytes = reader.readVarBytes();
        BinaryReader paramReader = new BinaryReader(new ByteArrayInputStream(paramBytes));
        byte[] method = paramReader.readVarBytes();
        String methodStr = new String(method);

        long size = paramReader.readVarInt();
        List<String> hashList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            byte[] bytes = paramReader.readBytes(32);
            String s = Helper.toHexString(bytes);
            hashList.add(s);
        }
        Map<String, Object> transactionMap = new HashMap<>();
        transactionMap.put("txHash", hash);
        transactionMap.put("contractAddress", contractStr);
        transactionMap.put("method", methodStr);
        transactionMap.put("hashSize", size);
        transactionMap.put("hashList", hashList);
        return transactionMap;
    }
}
