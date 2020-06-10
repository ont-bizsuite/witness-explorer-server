package io.ont.utils;

import com.github.ontio.OntSdk;
import com.github.ontio.account.Account;
import com.github.ontio.common.*;
import com.github.ontio.common.Helper;
import com.github.ontio.core.payload.InvokeWasmCode;
import com.github.ontio.core.transaction.Transaction;
import com.github.ontio.io.BinaryReader;
import com.github.ontio.io.BinaryWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * SDK 入口类
 *
 * @author 12146
 */
@Component
@Slf4j
public class SDKUtil {

    @Autowired
    ConfigParam param;
    @Autowired
    private ConfigParam configParam;
    private OntSdk wm;

    private OntSdk getOntSdk() throws Exception {
        if (wm == null) {
            wm = OntSdk.getInstance();
            wm.setRestful(param.RESTFUL_URL);
            wm.openWalletFile("wallet.json");
        }
        if (wm.getWalletMgr() == null) {
            wm.openWalletFile("wallet.json");
        }
        return wm;
    }

    public String checkOntIdDDO(String ontidStr) throws Exception {
        return getOntSdk().nativevm().ontId().sendGetDDO(ontidStr);
    }


    public Object checkEvent(String txHash) throws Exception {
        OntSdk ontSdk = getOntSdk();
        Object event = ontSdk.getConnect().getSmartCodeEvent(txHash);
        return event;
    }

    public Object getEventByHeight(int height) throws Exception {
        OntSdk ontSdk = getOntSdk();
        Object event = ontSdk.getConnect().getSmartCodeEvent(height);
        return event;
    }

    public Object sendTransaction(Transaction transaction, String acctWif, boolean preExec) throws Exception {
        OntSdk ontSdk = getOntSdk();
        Account account = new Account(Account.getPrivateKeyFromWIF(acctWif), ontSdk.getWalletMgr().getSignatureScheme());
        ontSdk.addSign(transaction, account);
        if (preExec) {
            return ontSdk.getConnect().sendRawTransactionPreExec(transaction.toHexString());
        } else {
            ontSdk.getConnect().sendRawTransaction(transaction.toHexString());
            return transaction.hash().toString();
        }
    }

    public Object sendPreTransaction(String params) throws Exception {
        OntSdk ontSdk = getOntSdk();
        Transaction[] txs = ontSdk.makeTransactionByJson(params);
        Object o = ontSdk.getConnect().sendRawTransactionPreExec(txs[0].toHexString());
        return o;
    }


    public String addTransactionSign(String txHex, String wif) throws Exception {
        Transaction transaction = Transaction.deserializeFrom(Helper.hexToBytes(txHex));
        OntSdk ontSdk = getOntSdk();
        Account account = new Account(Account.getPrivateKeyFromWIF(wif), ontSdk.getWalletMgr().getSignatureScheme());
        ontSdk.addSign(transaction, account);
        return transaction.toHexString();
    }


    public String createDataId() throws Exception {
        String pwd = UUID.randomUUID().toString();
        return getOntSdk().getWalletMgr().createIdentity(pwd).ontid;
    }

    public Transaction constructWasmTx(String contractHash, String method, List<Object> params, String payer, long gasLimit, long gasPrice) throws Exception {
        OntSdk ontSdk = getOntSdk();
        Address payerAddr = Address.decodeBase58(payer);
        Transaction invokeWasmCode = ontSdk.wasmvm().makeInvokeCodeTransaction(contractHash, method, params, payerAddr, gasLimit, gasPrice);
        return invokeWasmCode;
    }

    public List<String> getTxByHash(String hash) throws Exception {
        OntSdk ontSdk = getOntSdk();
        InvokeWasmCode transaction = (InvokeWasmCode) ontSdk.getConnect().getTransaction(hash);

        BinaryReader reader = new BinaryReader(new ByteArrayInputStream(transaction.invokeCode));
        byte[] contractBytes = reader.readBytes(20);
        String contractStr = Helper.toHexString(contractBytes);
        log.info(contractStr);
        byte[] paramBytes = reader.readVarBytes();
        BinaryReader paramReader = new BinaryReader(new ByteArrayInputStream(paramBytes));
        byte[] method = paramReader.readVarBytes();
        String methodStr = new String(method);
        log.info(methodStr);
        long l = paramReader.readVarInt();
        log.info("{}",l);
        List<String> hashList = new ArrayList<>();
        for (int i = 0; i < l; i++) {
            byte[] bytes = paramReader.readBytes(32);
            String s = Helper.toHexString(bytes);
            log.info(s);
            hashList.add(s);
        }
        return hashList;
    }

    public Object getTransaction(String hash) throws Exception {
        return getOntSdk().getConnect().getTransaction(hash);
    }
}
