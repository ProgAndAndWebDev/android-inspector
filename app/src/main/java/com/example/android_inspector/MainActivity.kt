package com.example.android_inspector

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Patterns
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebView.setWebContentsDebuggingEnabled
import android.webkit.WebViewClient
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.android_inspector.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private var adapterList: ArrayAdapter<String>? = null
    private val netData: MutableList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.listViewWeb.isVisible = false
        binding.textView.isVisible = false

        binding.listViewWeb.adapter =
            ArrayAdapter(this,R.layout.item_for_list,R.id.label,this.netData)
                .also { adapterList = it }

        binding.listViewWeb.onItemClickListener =
            OnItemClickListener { p1, p2, p3, p4 ->
                binding.webv.loadUrl(p1.getItemAtPosition(p3) as String) }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu,menu)

        val menuItem:MenuItem? = menu?.findItem(R.id.action_search)
        val searchView:SearchView = menuItem?.actionView as SearchView

        searchView.queryHint = "Enter URL..."
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextChange(newText: String?): Boolean {
                adapterList!!.clear()
                openWeb(" ")
                binding.textView.isVisible = false
                binding.listViewWeb.isVisible = false
               return false
            }
            override fun onQueryTextSubmit(urlinput: String?): Boolean {
                var url = urlinput
                val regex = "https?://.*".toRegex()
                if (!regex.containsMatchIn(url!!.trim { it <= ' ' })) {
                    url = "https://$url".trim { it <= ' ' }
                }
                if (Patterns.WEB_URL.matcher(url).matches()) {
                    adapterList!!.clear()
                    openWeb(url)
                } else {
                    adapterList!!.clear()
                    openWeb(" ")
                    binding.textView.isVisible = false
                    Toast.makeText(this@MainActivity, "Invalid URL...", Toast.LENGTH_LONG).show()
                }
                return false
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    private fun openWeb(url: String) =
        binding.webv.apply {
            getSettings().setJavaScriptEnabled(true)
            setWebContentsDebuggingEnabled(true)
            webViewClient = object : WebViewClient() {

                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    return super.shouldOverrideUrlLoading(view, request)
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    setTitle("Loading...")
                    super.onPageStarted(view, url, favicon)
                }

                override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?)
                : WebResourceResponse? {
                    view!!.post {
                        netData.add(request!!.url.toString())
                        adapterList?.notifyDataSetChanged()
                    }

                    return super.shouldInterceptRequest(view, request)
                }

            }
            binding.listViewWeb.isVisible = true
            binding.textView.isVisible = true
            loadUrl(url)
        }

}


