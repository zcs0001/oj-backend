package com.tree.backend.judge.codesandbox;

import com.tree.backend.judge.codesandbox.model.ExecuteCodeRequest;
import com.tree.backend.judge.codesandbox.model.ExecuteCodeResponse;

/**
 *  * 代码沙箱接口定义
 */
public interface CodeSandbox {

    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);

}
