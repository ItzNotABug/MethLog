package com.lazygeniouz.methlog.transform.asm;

import java.io.IOException;
import java.io.InputStream;

public interface IWeaver {

    boolean isWeavableClass(String filePath) throws IOException;

    byte[] weaveSingleClassToByteArray(InputStream inputStream) throws IOException;
}

