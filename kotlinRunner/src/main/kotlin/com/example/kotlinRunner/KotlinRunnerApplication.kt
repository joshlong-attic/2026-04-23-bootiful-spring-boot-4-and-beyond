package com.example.kotlinRunner

import org.springframework.beans.factory.BeanRegistrar
import org.springframework.beans.factory.BeanRegistry
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.core.env.Environment

@SpringBootApplication
class KotlinRunnerApplication

fun main(args: Array<String>) {
    runApplication<KotlinRunnerApplication>(*args)
}

class MyRegistrar : BeanRegistrar {
    override fun register(
        registry: BeanRegistry,
        env: Environment
    ) {

        supportsNull(null)
    }

    fun supportsNull ( str :String)  {

    }
}