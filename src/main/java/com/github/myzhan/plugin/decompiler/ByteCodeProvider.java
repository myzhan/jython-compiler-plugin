package com.github.myzhan.plugin.decompiler;

import org.jetbrains.java.decompiler.main.extern.IBytecodeProvider;

public class ByteCodeProvider implements IBytecodeProvider {

    public byte[] byteCode;

    ByteCodeProvider(byte[] byteCode) {
        this.byteCode = byteCode;
    }

    @Override
    public byte[] getBytecode(String externalPath, String internalPath) {
        return this.byteCode;
    }
}
