package com.SyndicG5.ui.ContainerHome.fragments.pitches

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.SyndicG5.R
import com.SyndicG5.databinding.ItemArticleImageBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import timber.log.Timber

class ProductImagesAdapter(private val images: List<String>?) :
    RecyclerView.Adapter<ProductImagesAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val itemView = ItemArticleImageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ImageViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        if (images?.isEmpty() == true) {
            holder.bind("")
        } else {
            holder.bind(images?.get(position))
        }

    }

    override fun getItemCount(): Int {
        val size = images?.size
        return if (size == null || size == 0) {
            Timber.e("COUNT: 1")
            1
        } else {
            Timber.e("COUNT: $size")
            size
        }
    }


    class ImageViewHolder(private val binding: ItemArticleImageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(imageUrl: String?) {
            Glide.with(binding.artileImg.context)
                .load(imageUrl)
                .placeholder(R.drawable.img_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.artileImg)
        }
    }

}
