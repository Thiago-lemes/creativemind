package com.studio.creativemind

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
	fromApplication<CreativemindApplication>().with(TestcontainersConfiguration::class).run(*args)
}
