package com.brandon.campingmate.song.presentation.board.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.brandon.campingmate.databinding.ItemPostBinding
import com.brandon.campingmate.databinding.ItemPostLoadingBinding
import com.brandon.campingmate.databinding.ItemPostUnknownBinding
import com.brandon.campingmate.song.domain.model.PostEntity
import com.brandon.campingmate.song.presentation.mapper.toPostEntity
import com.brandon.campingmate.song.utils.FirebaseUtils.toFormattedString

/**
 * 1. PostListAdapter
 * 2. DiffUtil
 * 3. abstract ViewHolder class -> ListAdapter 제네릭 타입 매개변수 지정
 * 4. abstract ViewHolder 를 상속한 Item, Loading, Unknown ViewHolder
 * 5. getItemViewType
 * 6. onCreateViewHolder
 * 7. onBindViewHolder
 * 8. onClickItem lambda
 * 9. Edit onCreateViewHolder
 * 10. Edit ItemViewHolder
 *
 * type - POSTITEM, LOADING, UNKNOWN
 * item - PostItem, Loading, (Unknown 없음)
 * viewModel - PostItemViewHolder, PostLoadingViewHolder, PostUnknownViewHolder
 *
 * ViewType 의 확장성을 고려한 type 처리
 */
class PostListAdapter(private val onClickItem: (PostEntity) -> Unit) :
    ListAdapter<PostListItem, PostListAdapter.PostViewHolder>(
        object : DiffUtil.ItemCallback<PostListItem>() {
            override fun areItemsTheSame(oldItem: PostListItem, newItem: PostListItem): Boolean {
                return when {
                    oldItem is PostListItem.PostItem && newItem is PostListItem.PostItem -> oldItem == newItem  // 모든 필드 비교
                    oldItem is PostListItem.Loading && newItem is PostListItem.Loading -> true  // Loading 은 object(싱글턴 객체)로 항상 같다
                    else -> false
                }
            }

            override fun areContentsTheSame(oldItem: PostListItem, newItem: PostListItem): Boolean {
                return when {
                    oldItem is PostListItem.PostItem && newItem is PostListItem.PostItem -> true    // 앞서 모든 필드 비교로 체크
                    oldItem is PostListItem.Loading && newItem is PostListItem.Loading -> true
                    else -> false
                }
            }
        }
    ) {


    abstract class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun onBind(item: PostListItem)
    }

    class PostItemViewHolder(
        private val binding: ItemPostBinding,
        private val onClickItem: (PostEntity) -> Unit
    ) : PostViewHolder(binding.root) {
        override fun onBind(item: PostListItem) = with(binding) {
            if (item is PostListItem.PostItem) {
                tvTitle.text = item.title
                tvContent.text = item.content
                tvTimestamp.text = item.timestamp.toFormattedString()
                val imageUrl = item.imageUrlList?.firstOrNull()
                if (imageUrl == null) {
                    ivPostImage.isVisible = false
                } else {
                    ivPostImage.load(imageUrl)
                }
            }
            binding.root.setOnClickListener {
                if (item is PostListItem.PostItem) {
                    onClickItem(item.toPostEntity())
                }
            }
        }
    }

    class PostLoadingViewHolder(private val binding: ItemPostLoadingBinding) : PostViewHolder(binding.root) {
        override fun onBind(item: PostListItem) = Unit
    }

    class PostUnknownViewHolder(binding: ItemPostUnknownBinding) : PostViewHolder(binding.root) {
        override fun onBind(item: PostListItem) = Unit  // 바인딩 없음
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is PostListItem.PostItem -> PostListViewType.POSTITEM
            is PostListItem.Loading -> PostListViewType.LOADING
            else -> PostListViewType.UNKNOWN
        }.ordinal
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        return when (PostListViewType.from(viewType)) {
            PostListViewType.POSTITEM -> PostItemViewHolder(
                ItemPostBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ),
                onClickItem
            )

            PostListViewType.LOADING -> PostLoadingViewHolder(
                ItemPostLoadingBinding.inflate(
                    LayoutInflater.from(
                        parent.context
                    ), parent, false
                )
            )

            else -> PostUnknownViewHolder(
                ItemPostUnknownBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

}