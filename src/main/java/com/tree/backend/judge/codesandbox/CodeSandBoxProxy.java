package com.tree.backend.judge.codesandbox;

import com.tree.backend.judge.codesandbox.model.ExecuteCodeRequest;
import com.tree.backend.judge.codesandbox.model.ExecuteCodeResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 使用代理模式，提供一个Proxy，用来增强代码沙箱的能力
 * 这段代码实现了代码沙箱的代理模式，通过代理模式可以在代码沙箱的执行前后增加一些额外的操作。在这里，代理模式被用来记录执行请求和响应的日志。
 * 这个代理类 CodeSandBoxProxy 实现了 CodeSandbox 接口，并包含一个成员变量 codeSandBox，代表真正的代码沙箱实例。
 * 在 executeCode 方法中，它在执行前记录了请求信息，然后调用真正的代码沙箱实例的 executeCode 方法，最后在执行后记录了响应信息。
 * 这样，通过使用代理模式，可以在不修改原有代码沙箱逻辑的基础上，增加了日志记录的功能。
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
