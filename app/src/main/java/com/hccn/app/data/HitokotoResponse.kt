package com.hccn.app.data

data class HitokotoResponse(
    // 一言标识
    val id: Int,
    // 一言正文。编码方式 unicode。使用 utf-8。
    val hitokoto: String,
    // 类型。请参考https://developer.hitokoto.cn/sentence/#%E5%8F%A5%E5%AD%90%E7%B1%BB%E5%9E%8B-%E5%8F%82%E6%95%B0
    val type: String,
    // 一言的出处
    val from: String,
    // 一言的作者
    val from_who: String,
    // 添加者
    val creator: String,
    // 添加者用户标识
    val creator_uid: Int,
    // 审核员标识
    val reviewer: Int,
    // 一言唯一标识；可以链接到 https://hitokoto.cn?uuid=[uuid] (opens new window)查看这个一言的完整信息
    val uuid: String,
    // 提交方式
    val commit_from: String,
    // 添加时间
    val created_at: String,
    // 句子长度
    val length: Int,
)