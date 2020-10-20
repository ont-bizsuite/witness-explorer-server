package io.ont.controller.vo;

import com.github.ontio.common.UInt256;
import lombok.Data;

import java.util.Arrays;


@Data
public class Proof {
    public UInt256 root;
    public int size, blockheight, index, leafHeight;
    public UInt256[] proof;
    public String txHash;

    public Proof(UInt256 root, int size, int blockheight, int leafHeight, int index, UInt256[] proof, String txHash) {
        this.root = root;
        this.size = size;
        this.blockheight = blockheight;
        this.leafHeight = leafHeight;
        this.txHash = txHash;
        this.index = index;
        this.proof = proof;
    }

    public UInt256 getRoot() {
        return root;
    }

    public int getSize() {
        return size;
    }

    public int getBlockHeight() {
        return blockheight;
    }

    public int getLeafHeight() {
        return leafHeight;
    }

    public String getTxHash() {
        return txHash;
    }

    public int getIndex() {
        return index;
    }

    public UInt256[] getProof() {
        return proof;
    }

    @Override
    public String toString() {
        return String.format("block height: %d, leaf height: %d, size: %d, index: %d, root: %s, proof: %s, tx hash:%s", blockheight, leafHeight, size, index, root, Arrays.toString(proof), txHash);
    }
}