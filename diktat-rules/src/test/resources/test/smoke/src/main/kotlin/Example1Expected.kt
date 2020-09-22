/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package org.cqfn.diktat.test.smoke

class Example {
    @get:JvmName ("getIsValid")
    val isValid = true
    val foo: Int =1
    /**
     * @param x
     * @param y
     * @return
     */
    fun bar(x: Int, y: Int) = x + y

    /**
     * @param sub
     * @return
     */
    fun String.countSubStringOccurrences(sub: String): Int {
        // println("sub: $sub")
        return this.split(sub).size - 1
    }

    /**
     * @return
     */
    fun String.splitPathToDirs(): List<String> =
        this
            .replace("\\", "/")
            .replace("//", "/")
            .split("/")
}

