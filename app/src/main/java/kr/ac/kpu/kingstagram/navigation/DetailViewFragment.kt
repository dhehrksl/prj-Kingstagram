package kr.ac.kpu.kingstagram.navigation

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import com.hendraanggrian.appcompat.widget.SocialView
import kotlinx.android.synthetic.main.cardview_detail.view.*
import kotlinx.android.synthetic.main.fragment_detail.view.*
import kr.ac.kpu.kingstagram.CommentsActivity
import kr.ac.kpu.kingstagram.PostView
import kr.ac.kpu.kingstagram.R
import java.text.SimpleDateFormat


class DetailViewFragment : Fragment() {
    var firestore: FirebaseFirestore? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = LayoutInflater.from(activity).inflate(R.layout.fragment_detail, container, false)
        firestore = FirebaseFirestore.getInstance()

        view.recycleView_detail.layoutManager = LinearLayoutManager(activity)
        view.recycleView_detail.adapter = PostViewAdapter()

        /*
        val user = Firebase.auth.currentUser
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document("${user?.uid}")
            .get()
            .addOnSuccessListener { result ->
                //if (result.id == "name") {
                    Toast.makeText(this.context,"${result.data?.get("name")}",Toast.LENGTH_LONG)
                    view.textView01.text = "${result.data?.get("name")}"
                //}
                //if (result.id == "nickname") {
                    view.textView02.text = "${result.data?.get("nickname")}"


                //Log.w("TAG", "${result}")
            }
            .addOnFailureListener { exception ->
                //Log.w(TAG, "Error getting documents.", exception)
            }*/
        return view
    }

    inner class PostViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var contentList: ArrayList<PostView> = arrayListOf()
        var contentUidList: ArrayList<String> = arrayListOf()

        init {
            firestore?.collection("posts")?.orderBy("date", Query.Direction.DESCENDING)
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    contentList.clear()
                    contentUidList.clear()
                    for (snapshot in querySnapshot!!.documents) {
                        var item = snapshot.toObject(PostView::class.java)
                        contentList.add(item!!)
                        /*var content: String = "${snapshot.data?.get("content")}"
                        var imageUrl: String = "${snapshot.data?.get("imageUrl")}"
                        var kingcount: Int = "${snapshot.data?.get("kingcount")}".toInt()
                        var uid : String = snapshot.id
                        var userId: String = "${snapshot.data?.get("userId")}"
                        contentList.add(PostView(content, imageUrl, kingcount, uid, userId))*/

                        contentUidList.add(snapshot.id)
                    }
                    notifyDataSetChanged()
                }
        }

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(p0.context).inflate(R.layout.cardview_detail, p0, false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return contentList.size
        }

        @SuppressLint("ResourceAsColor")
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
            var viewholder = (p0 as CustomViewHolder).itemView
            val user = Firebase.auth.currentUser
            val db = FirebaseFirestore.getInstance()
            val dateForm = SimpleDateFormat("yyyy.MM.dd  HH:mm")

            viewholder.card_view_detail_titleView.text = contentList!![p1].userId

                Glide.with(p0.itemView.context).load(contentList!![p1].imageUrl)
                    .into(viewholder.card_view_detail_imageView)

                for (i in contentList!![p1].kings.keys) {
                    if (user?.uid == i) {
                        if (contentList!![p1].kings[i] ?: error(""))
                            viewholder.card_view_detail_kingImg.setImageResource(R.drawable.kingking)
                        else
                            viewholder.card_view_detail_kingImg.setImageResource(R.drawable.no_king)
                    }
            }
            for (i in contentList!![p1].unkings.keys) {
                if (user?.uid == i) {
                    if (contentList!![p1].unkings[i] ?: error(""))
                        viewholder.card_view_detail_sad_kingImg.setImageResource(R.drawable.sad_king)
                    else
                        viewholder.card_view_detail_sad_kingImg.setImageResource(R.drawable.happy_king)
                }
            }

            viewholder.card_view_detail_contentView.text = contentList!![p1].content

            viewholder.card_view_detail_kingView.text = "King  " + contentList!![p1].like + "개"

            viewholder.card_view_detail_timeView.text = "${dateForm.format(contentList!![p1].date.toDate())}"

            viewholder.card_view_detail_commentsView.text =
                "${contentList!![p1].comments.size} 개의 댓글 모두 보기"

            viewholder.card_view_detail_tagLayout.removeAllViews()
            for (i in contentList!![p1].tag) {
                val textView = TextView(activity)
                textView.text = "$i"
                textView.setTextColor(Color.parseColor("#FCCA3A"))
                viewholder.card_view_detail_tagLayout.addView(textView)
            }

            // detail comments activity button listener
            viewholder.card_view_detail_commentsView.setOnClickListener {
                println("${contentUidList!![p1]}")
                var intent = Intent(context, CommentsActivity::class.java)
                intent.putExtra("userId", contentList!![p1].userId)
                intent.putExtra("content", contentList!![p1].content)
                intent.putExtra("date", dateForm.format(contentList!![p1].date.toDate()))
                intent.putExtra("tag", contentList!![p1].tag)
                intent.putExtra("uid", contentUidList!![p1])
                //intent.putExtra("comments", contentList!![p1].comments as HashMap<String, String>)
                startActivity(intent)

            }
            // kings button listener
            viewholder.card_view_detail_kingImg.setOnClickListener {
                var count = 0
                for (i in contentList!![p1].kings.keys) {
                    if (user?.uid == i) {
                        count++
                        if (contentList!![p1].kings[i] ?: error("")) {
                            val data = hashMapOf(
                                "kings" to mapOf("${user?.uid}" to false),
                                "like" to contentList!![p1].like - 1
                            )
                            db.collection("posts").document("${contentUidList!![p1]}")
                                .set(data, SetOptions.merge())
                        } else {
                            val data = hashMapOf(
                                "kings" to mapOf("${user?.uid}" to true),
                                "like" to contentList!![p1].like + 1
                            )
                            db.collection("posts").document("${contentUidList!![p1]}")
                                .set(data, SetOptions.merge())
                        }
                    }
                }
                if (count == 0) {
                    val data = hashMapOf(
                        "kings" to mapOf("${user?.uid}" to true),
                        "like" to contentList!![p1].like + 1
                    )
                    db.collection("posts").document("${contentUidList!![p1]}")
                        .set(data, SetOptions.merge())
                }
            }
            // sad kings button listener
            viewholder.card_view_detail_sad_kingImg.setOnClickListener {
                var count = 0
                for (i in contentList!![p1].unkings.keys) {
                    if (user?.uid == i) {
                        count++
                        if (contentList!![p1].unkings[i] ?: error("")) {
                            val data = hashMapOf(
                                "unkings" to mapOf("${user?.uid}" to false),
                                "unlike" to contentList!![p1].unlike - 1
                            )
                            db.collection("posts").document("${contentUidList!![p1]}")
                                .set(data, SetOptions.merge())
                        } else {
                            val data = hashMapOf(
                                "unkings" to mapOf("${user?.uid}" to true),
                                "unlike" to contentList!![p1].unlike + 1
                            )
                            db.collection("posts").document("${contentUidList!![p1]}")
                                .set(data, SetOptions.merge())
                        }
                    }
                }
                if (count == 0) {
                    val data = hashMapOf(
                        "unkings" to mapOf("${user?.uid}" to true),
                        "unlike" to contentList!![p1].unlike + 1
                    )
                    db.collection("posts").document("${contentUidList!![p1]}")
                        .set(data, SetOptions.merge())
                }
            }


        }

    }
}
