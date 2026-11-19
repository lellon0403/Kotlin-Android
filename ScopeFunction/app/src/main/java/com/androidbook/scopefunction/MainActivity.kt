package com.androidbook.scopefunction

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Person {
    var name: String = ""
    var age: Int = 0
}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val text1: String? = "Kotlin"
        text1?.let {
            println("이름의 길이는 ${it.length}입니다.")
        }

        val text2: String? = "Hello, Kotlin"
        val messageLength = text2?.run {
            println("이름은 $this 입니다.")
            length
        }
        println("길이: $messageLength")

        val result = with(StringBuilder()) {
            append("Hello, ")
            append("Kotlin!")
            toString()
        }
        println(result)

        val person = Person().apply {
            name = "John"
            age = 25
        }
        println(person.name)

        val number = mutableListOf(1, 2, 3).also {
            println("리스트의 크기: ${it.size}")
            it.add(4)
        }
        println(number)
    }
}