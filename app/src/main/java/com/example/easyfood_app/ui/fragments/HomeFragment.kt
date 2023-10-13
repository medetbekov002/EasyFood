package com.example.easyfood_app.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.easyfood_app.R
import com.example.easyfood_app.adapters.CategoriesRecyclerAdapter
import com.example.easyfood_app.adapters.MostPopularRecyclerAdapter
import com.example.easyfood_app.adapters.OnItemClick
import com.example.easyfood_app.adapters.OnLongItemClick
import com.example.easyfood_app.data.pojo.Category
import com.example.easyfood_app.data.pojo.Meal
import com.example.easyfood_app.data.pojo.MealDetail
import com.example.easyfood_app.data.pojo.RandomMealResponse
import com.example.easyfood_app.databinding.FragmentHomeBinding
import com.example.easyfood_app.extensions.Constants.Companion.CATEGORY_NAME
import com.example.easyfood_app.extensions.Constants.Companion.MEAL_AREA
import com.example.easyfood_app.extensions.Constants.Companion.MEAL_ID
import com.example.easyfood_app.extensions.Constants.Companion.MEAL_NAME
import com.example.easyfood_app.extensions.Constants.Companion.MEAL_STR
import com.example.easyfood_app.extensions.Constants.Companion.MEAL_THUMB
import com.example.easyfood_app.mvvm.DetailsMVVM
import com.example.easyfood_app.mvvm.MainFragMVVM
import com.example.easyfood_app.ui.activites.MealActivity
import com.example.easyfood_app.ui.activites.MealDetailsActivity
import com.example.easyfood_app.ui.MealBottomDialog

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var meal: RandomMealResponse
    private lateinit var detailMvvm: DetailsMVVM
    private lateinit var myAdapter: CategoriesRecyclerAdapter
    private lateinit var mostPopularFoodAdapter: MostPopularRecyclerAdapter
    private var randomMealId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        detailMvvm = ViewModelProvider(this).get(DetailsMVVM::class.java)
        binding = FragmentHomeBinding.inflate(layoutInflater)
        myAdapter = CategoriesRecyclerAdapter()
        mostPopularFoodAdapter = MostPopularRecyclerAdapter()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mainFragMVVM = ViewModelProvider(this).get(MainFragMVVM::class.java)
        showLoadingCase()

        prepareCategoryRecyclerView()
        preparePopularMeals()
        onRandomMealClick()
        onRandomLongClick()

        mainFragMVVM.observeMealByCategory().observe(viewLifecycleOwner, Observer { t ->
            t?.meals?.let {
                setMealsByCategoryAdapter(it)
                cancelLoadingCase()
            }
        })

        mainFragMVVM.observeCategories().observe(viewLifecycleOwner, Observer { t ->
            t?.categories?.let {
                setCategoryAdapter(it)
            }
        })

        mainFragMVVM.observeRandomMeal().observe(viewLifecycleOwner, Observer { t ->
            t?.meals?.let {
                val mealImage = view.findViewById<ImageView>(R.id.img_random_meal)
                val imageUrl = it[0].strMealThumb
                randomMealId = it[0].idMeal
                Glide.with(this@HomeFragment)
                    .load(imageUrl)
                    .into(mealImage)
                meal = t
            }
        })

        mostPopularFoodAdapter.setOnClickListener(object : OnItemClick {
            override fun onItemClick(meal: Meal) {
                val intent = Intent(activity, MealDetailsActivity::class.java).apply {
                    putExtra(MEAL_ID, meal.idMeal)
                    putExtra(MEAL_STR, meal.strMeal)
                    putExtra(MEAL_THUMB, meal.strMealThumb)
                }
                startActivity(intent)
            }
        })

        myAdapter.onItemClicked(object : CategoriesRecyclerAdapter.OnItemCategoryClicked {
            override fun onClickListener(category: Category) {
                val intent = Intent(activity, MealActivity::class.java).apply {
                    putExtra(CATEGORY_NAME, category.strCategory)
                }
                startActivity(intent)
            }
        })

        mostPopularFoodAdapter.setOnLongCLickListener(object : OnLongItemClick {
            override fun onItemLongClick(meal: Meal) {
                detailMvvm.getMealByIdBottomSheet(meal.idMeal)
            }
        })

        detailMvvm.observeMealBottomSheet()
            .observe(viewLifecycleOwner, Observer { t ->
                t?.getOrNull(0)?.let {
                    val bottomSheetFragment = MealBottomDialog()
                    val b = Bundle().apply {
                        putString(CATEGORY_NAME, it.strCategory)
                        putString(MEAL_AREA, it.strArea)
                        putString(MEAL_NAME, it.strMeal)
                        putString(MEAL_THUMB, it.strMealThumb)
                        putString(MEAL_ID, it.idMeal)
                    }
                    bottomSheetFragment.arguments = b
                    bottomSheetFragment.show(childFragmentManager, "BottomSheetDialog")
                }
            })

        // on search icon click
        binding.imgSearch.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
        }
    }

    private fun onRandomMealClick() {
        binding.randomMeal.setOnClickListener {
            meal.meals?.getOrNull(0)?.let {
                val intent = Intent(activity, MealDetailsActivity::class.java).apply {
                    putExtra(MEAL_ID, it.idMeal)
                    putExtra(MEAL_STR, it.strMeal)
                    putExtra(MEAL_THUMB, it.strMealThumb)
                }
                startActivity(intent)
            }
        }
    }

    private fun onRandomLongClick() {
        binding.randomMeal.setOnLongClickListener {
            detailMvvm.getMealByIdBottomSheet(randomMealId)
            true
        }
    }

    private fun showLoadingCase() {
        binding.apply {
            header.visibility = View.INVISIBLE
            tvWouldLikeToEat.visibility = View.INVISIBLE
            randomMeal.visibility = View.INVISIBLE
            tvOverPupItems.visibility = View.INVISIBLE
            recViewMealsPopular.visibility = View.INVISIBLE
            tvCategory.visibility = View.INVISIBLE
            categoryCard.visibility = View.INVISIBLE
            loadingGif.visibility = View.VISIBLE
            rootHome.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.g_loading))
        }
    }

    private fun cancelLoadingCase() {
        binding.apply {
            header.visibility = View.VISIBLE
            tvWouldLikeToEat.visibility = View.VISIBLE
            randomMeal.visibility = View.VISIBLE
            tvOverPupItems.visibility = View.VISIBLE
            recViewMealsPopular.visibility = View.VISIBLE
            tvCategory.visibility = View.VISIBLE
            categoryCard.visibility = View.VISIBLE
            loadingGif.visibility = View.INVISIBLE
            rootHome.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
        }
    }

    private fun setMealsByCategoryAdapter(meals: List<Meal>) {
        mostPopularFoodAdapter.setMealList(meals)
    }

    private fun setCategoryAdapter(categories: List<Category>) {
        myAdapter.setCategoryList(categories)
    }

    private fun prepareCategoryRecyclerView() {
        binding.recyclerView.apply {
            adapter = myAdapter
            layoutManager = GridLayoutManager(context, 3, GridLayoutManager.VERTICAL, false)
        }
    }

    private fun preparePopularMeals() {
        binding.recViewMealsPopular.apply {
            adapter = mostPopularFoodAdapter
            layoutManager = GridLayoutManager(context, 1, GridLayoutManager.HORIZONTAL, false)
        }
    }
}
