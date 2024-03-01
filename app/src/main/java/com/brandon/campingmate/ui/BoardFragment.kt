package com.brandon.campingmate.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.brandon.campingmate.data.PostListItem
import com.brandon.campingmate.databinding.FragmentBoardBinding

class BoardFragment : Fragment() {

    private var _binding: FragmentBoardBinding? = null
    private val binding get() = _binding!!

    private val postListAdapter: PostListAdapter by lazy { PostListAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBoardBinding.inflate(inflater, container, false)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
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