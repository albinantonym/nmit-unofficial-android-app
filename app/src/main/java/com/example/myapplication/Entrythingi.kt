package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

//Hello, i made the app and i've given explanation only for MainActivity.kt read at ur own pace
//if ur interested in contrbuting to fix bugs or add any new feature, feel free to make a PR

class Entrythingi : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.entry)
        val inf=findViewById<ImageButton>(R.id.info)
        inf.setOnClickListener {
            val i = Intent(this@Entrythingi, com.example.myapplication.Info::class.java)
            startActivity(i)
        }
        val con=findViewById<ConstraintLayout>(R.id.con)
        con.setOnClickListener{
            val i = Intent(this@Entrythingi, com.example.myapplication.MainActivity::class.java)
            startActivity(i)
        }
        val notes=findViewById<ConstraintLayout>(R.id.notes)
        notes.setOnClickListener{
            val i= Intent("android.intent.action.VIEW", Uri.parse("https://drive.google.com/drive/folders/1EEZ5EAIiZoo5eP4nWuBT8HF41FyKdrdu"));
            startActivity(i);
        }
        val leet=findViewById<ConstraintLayout>(R.id.Leet)
        leet.setOnClickListener{
            val i = Intent(this@Entrythingi, com.example.myapplication.Leetcode::class.java)
            startActivity(i);
        }
        val qp=findViewById<ConstraintLayout>(R.id.PYP)
        qp.setOnClickListener{
            val i= Intent("android.intent.action.VIEW", Uri.parse("https://drive.google.com/drive/folders/1FHtWB68knBfFjXqIyurXlsuLaCaICDIX"));
            startActivity(i);
        }
    }
}
