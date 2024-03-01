package com.brandon.campingmate.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.brandon.campingmate.data.PostListItem
import com.brandon.campingmate.databinding.FragmentBoardBinding
import timber.log.Timber

class BoardFragment : Fragment() {

    private var _binding: FragmentBoardBinding? = null
    private val binding get() = _binding!!

    private val postListAdapter: PostListAdapter by lazy { PostListAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBoardBinding.inflate(inflater, container, false)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initListener()
    }

    private fun initListener() = with(binding) {
        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                Timber.d("Search submitted: $query")
                // TODO: Implement search logic here
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                btnWrite.isVisible = newText.isNullOrEmpty()
                return false
            }
        })
    }

    private fun initView() = with(binding) {

        rvPostList.adapter = postListAdapter
        rvPostList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        postListAdapter.submitList(
            listOf(
                PostListItem.PostItem(),
                PostListItem.PostItem(),
                PostListItem.PostItem(),
                PostListItem.PostItem(),
                PostListItem.Loading,
                PostListItem.PostItem(),
                PostListItem.PostItem(),
            )
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = BoardFragment()
    }
}