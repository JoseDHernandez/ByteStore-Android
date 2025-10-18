package com.example.bytestore.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.example.bytestore.R
import com.example.bytestore.databinding.ViewTopBarBinding

class TopBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?=null,
    defStyleAttr:Int=0
): FrameLayout(context,attrs,defStyleAttr) {
private val binding= ViewTopBarBinding.inflate(LayoutInflater.from(context),this,true)
    init {
       val attributes= context.obtainStyledAttributes(attrs, R.styleable.TopBarView)
        binding.viewTitle.text = attributes.getString(R.styleable.TopBarView_titleText) ?: "Volver"
        attributes.recycle()
    }
    //Establer titulo
    fun setTitle(title: String){
        binding.viewTitle.text = title
    }
    //Boton de regreso
    fun setOnBackClickListener(action: () -> Unit) {
        binding.buttonBack.setOnClickListener { action() }
    }

}