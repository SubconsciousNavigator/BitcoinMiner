package com.advait.bmwproject.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.Nullable
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

import com.advait.bmwproject.R
import com.advait.bmwproject.R.layout.*
import com.advait.bmwproject.databinding.ActivityMainBinding
import com.advait.bmwproject.viewmodel.BitcoinMineViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var bitcoinMineViewModel: BitcoinMineViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activity_main)
        setSupportActionBar(toolbar)
        bitcoinMineViewModel =
            ViewModelProviders.of(this).get(BitcoinMineViewModel::class.java)
        val binding: ActivityMainBinding =
            DataBindingUtil.setContentView(this, activity_main)
        binding.data = bitcoinMineViewModel
        binding.setLifecycleOwner(this)
        bitcoinMineViewModel.getBitCoinCount().observe(this, Observer(){
            textView.text = "BTC $it"
        })
        button.setOnClickListener {
            if (bitcoinMineViewModel.login()) {
                mineSomeBitcoins()
            } else {
                connectToCloud()
            }
        }
    }

    private fun mineSomeBitcoins() {
        if (bitcoinMineViewModel.isInputHashDataAvailable()) {
            fetchDataFromCloud()
        } else {
            calculateHashAndSendToCloud()
        }
    }

    private fun calculateHashAndSendToCloud() {
        bitcoinMineViewModel.calculateHashAndSendToCloud()
    }

    private fun fetchDataFromCloud(): Boolean {
        return bitcoinMineViewModel.fetchWork()
    }

    private fun connectToCloud(): Boolean {
        return bitcoinMineViewModel.login()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> connectToCloud()
            else -> super.onOptionsItemSelected(item)
        }
    }
}
