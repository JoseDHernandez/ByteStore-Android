package com.example.bytestore.ui.product

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bytestore.R

// Data class para reviews
data class Review(
    val id: Long,
    val author: String,
    val date: String,
    val rating: Int,
    val comment: String
)

class ReviewsAdapter : ListAdapter<Review, ReviewsAdapter.ReviewVH>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Review>() {
            override fun areItemsTheSame(oldItem: Review, newItem: Review): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Review, newItem: Review): Boolean =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewVH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_review, parent, false)
        return ReviewVH(v)
    }

    override fun onBindViewHolder(holder: ReviewVH, position: Int) {
        holder.bind(getItem(position))
    }

    class ReviewVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(R.id.reviewerName)
        private val date: TextView = itemView.findViewById(R.id.reviewDate)
        private val comment: TextView = itemView.findViewById(R.id.reviewText)
        private val ratingContainer: ViewGroup = itemView.findViewById(R.id.ratingContainer)

        fun bind(r: Review) {
            name.text = r.author
            date.text = r.date
            comment.text = r.comment
            // Ajustar estrellas: remplazar imagenes segun rating
            for (i in 0 until ratingContainer.childCount) {
                val child = ratingContainer.getChildAt(i)
                if (child is ImageView) {
                    val res = if (i < r.rating) R.drawable.icon_star else R.drawable.icon_star_empty
                    child.setImageResource(res)
                }
            }
        }
    }
}

