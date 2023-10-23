package com.tree.backend.judge.codesandbox;

import com.tree.backend.judge.codesandbox.model.ExecuteCodeRequest;
import com.tree.backend.judge.codesandbox.model.ExecuteCodeResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 使用代理模式，提供一个Proxy，来增强代码沙箱的能力
 */
@Slf4j
public class CodeSandBoxProxy implements CodeSandbox{

    private final CodeSandbox codeSandBox;

    public CodeSandBoxProxy(CodeSandbox codeSandBox){
        this.codeSandBox = codeSandBox;
    }

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        log.info("代码沙箱请求信息" + executeCodeRequest.toString());
        ExecuteCodeResponse executeCodeResponse = codeSandBox.executeCode(executeCodeRequest);
        log.info("代码沙箱响应信息" + executeCodeResponse.toString());
        return executeCodeResponse;
    }

}
