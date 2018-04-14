package com.jiubao.testlayout

import android.content.Context
import android.graphics.Bitmap
import android.opengl.Visibility
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jiubao.testintent.R
import kotlinx.android.synthetic.main.activity_layout_root.*
import android.support.v7.widget.RecyclerView.ViewHolder
import android.os.AsyncTask.execute
import android.view.ContextMenu
import android.widget.*


/**
 * 功能描述:
 * 作者： Administrator 时间： 2018/4/12.
 * 版本:
 */
class LayoutRootAct:AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_layout_root)
//        val adapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, arrayOf("1","2","3"))
//        list1.adapter = adapter
//        list1.onItemClickListener = AdapterView.OnItemClickListener({parent,  v,  position,  id ->
//
//        })

//        recyclerview.apply {
//            setHasFixedSize(true)
//            layoutManager = LinearLayoutManager(this@LayoutRootAct)
//            adapter = MyAdapter(arrayOf("1","2","3","4","5","1","2","3","4",
//                    "5","1","2","3","4","5","1","2","3","4","5","1","2","3","4","5","1","2","3","4","5"))
//        }
//        button4.setOnClickListener {
//            stub_import.inflate()
////            stub_import.visibility = View.VISIBLE
//        }
//        gridlayout.adapter = ImageAdapter(this)


    }

    override fun onResume() {
        super.onResume()

//
//        textView.postDelayed({
//            val left1 = textView.left
//            val left2 = button2.left
//
//            val top1 = textView.top
//            val top2 = button2.top
//
//            Log.w("test1","$left1 $left2 $top1 $top2")
//            val aa = 10;
//        },1000)
//
//        val left1 = textView.measuredWidth
//        val left2 = button2.measuredWidth
//
//        val top1 = textView.measuredHeight
//        val top2 = button2.measuredHeight
//        Log.w("test","$left1 $left2 $top1 $top2")
//        val aa = 10;
    }
    class MyViewHolder(val textView:TextView):RecyclerView.ViewHolder(textView)

    class MyAdapter(val data:Array<String>):RecyclerView.Adapter<MyViewHolder>(){
        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MyViewHolder {
            val textView:TextView = LayoutInflater.from(parent?.context).inflate(android.R.layout.simple_list_item_1,parent,false) as TextView
            return MyViewHolder(textView)
        }

        override fun getItemCount() = data.size

        override fun onBindViewHolder(holder: MyViewHolder?, position: Int) {
            holder?.textView?.text = data[position]
        }
    }




    class ImageAdapter(private val context: Context):BaseAdapter(){
        private val mThumbIds = arrayOf<Int>(
                R.drawable.ic_launcher_background, R.drawable.ic_launcher_background,
                R.drawable.ic_launcher_background, R.drawable.ic_launcher_background,
                R.drawable.ic_launcher_background, R.drawable.ic_launcher_background,
                R.drawable.ic_launcher_background, R.drawable.ic_launcher_background,
                R.drawable.ic_launcher_background, R.drawable.ic_launcher_background,
                R.drawable.ic_launcher_background, R.drawable.ic_launcher_background,
                R.drawable.ic_launcher_background, R.drawable.ic_launcher_background,
                R.drawable.ic_launcher_background, R.drawable.ic_launcher_background,
                R.drawable.ic_launcher_background, R.drawable.ic_launcher_background,
                R.drawable.ic_launcher_background, R.drawable.ic_launcher_background,
                R.drawable.ic_launcher_background, R.drawable.ic_launcher_background,
                R.drawable.ic_launcher_background, R.drawable.ic_launcher_background)

        override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
            val image:ImageView
            if(p1 == null){
                image = ImageView(context).apply {
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    layoutParams = ViewGroup.LayoutParams(85,85)
                    setPadding(8,8,8,8)
                }
            }else{
                image = p1!! as ImageView
            }
            image.setImageResource(mThumbIds[p0])
            return image
        }

        override fun getItem(p0: Int) = null
        override fun getItemId(p0: Int) = 0L
        override fun getCount() = mThumbIds.size

    }

}