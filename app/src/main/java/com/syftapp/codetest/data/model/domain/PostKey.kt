package com.syftapp.codetest.data.model.domain

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "post_keys")
data class PostKey(@PrimaryKey val id: Long, val previousKey: Int?, val nextKey: Int?)