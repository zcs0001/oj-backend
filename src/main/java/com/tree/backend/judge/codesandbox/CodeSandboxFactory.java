package com.tree.backend.judge.codesandbox;

import com.tree.backend.judge.codesandbox.impl.ExampleCodeSandbox;
import com.tree.backend.judge.codesandbox.impl.RemoteCodeSandbox;
import com.tree.backend.judge.codesandbox.impl.ThirdPartyCodeSandbox;

public class CodeSandboxFactory {

    /**
     * 创建代码沙箱实例
     *
     * @param type 沙箱类型
     * @return
     */
    public static CodeSandbox newInstance(String type) {
        switch (type) {
            case "example":
                return new ExampleCodeSandbox();
            case "remote":
                return new RemoteCodeSandbox();
            case "thirdParty":
                return new ThirdPartyCodeSandbox();
            default:
                return new ExampleCodeSandbox();
        }
    }
}
