package com.example.easyfood_app.ui.activites

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.example.easyfood_app.R
import com.example.easyfood_app.data.pojo.Meal
import com.example.easyfood_app.mvvm.MealActivityMVVM
import com.example.easyfood_app.extensions.Constants.Companion.CATEGORY_NAME
import com.example.easyfood_app.extensions.Constants.Companion.MEAL_ID
import com.example.easyfood_app.extensions.Constants.Companion.MEAL_STR
import com.example.easyfood_app.extensions.Constants.Companion.MEAL_THUMB
import com.example.easyfood_app.adapters.MealRecyclerAdapter
import com.example.easyfood_app.adapters.SetOnMealClickListener
import com.example.easyfood_app.databinding.ActivityCategoriesBinding

class MealActivity : AppCompatActivity() {
    private lateinit var mealActivityMvvm: MealActivityMVVM
    private lateinit var binding: ActivityCategoriesBinding
    private lateinit var myAdapter: MealRecyclerAdapter
    private var categoryName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mealActivityMvvm = ViewModelProviders.of(this).get(MealActivityMVVM::class.java)
        startLoading()
        prepareRecyclerView()
        mealActivityMvvm.getMealsByCategory(getCategory())
        mealActivityMvvm.observeMeal().observe(this, Observer { meals ->
            if (meals == null) {
                hideLoading()
                Toast.makeText(applicationContext, "No meals in this category", Toast.LENGTH_SHORT).show()
                onBackPressed()
            } else {
                myAdapter.setCategoryList(meals)
                binding.tvCategoryCount.text = "$categoryName : ${meals.size}"
                hideLoading()
            }
        })

        myAdapter.setOnMealClickListener(object : SetOnMealClickListener {
            override fun setOnClickListener(meal: Meal) {
                val intent = Intent(applicationContext, MealDetailsActivity::class.java).apply {
                    putExtra(MEAL_ID, meal.idMeal)
                    putExtra(MEAL_STR, meal.strMeal)
                    putExtra(MEAL_THUMB, meal.strMealThumb)
                }
                startActivity(intent)
            }
        })
    }

    private fun hideLoading() {
        binding.loadingGifMeals.visibility = View.INVISIBLE
        binding.mealRoot.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.white))
    }

    private fun startLoading() {
        binding.loadingGifMeals.visibility = View.VISIBLE
        binding.mealRoot.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.g_loading))
    }

    private fun getCategory(): String {
        val tempIntent = intent
        val x = intent.getStringExtra(CATEGORY_NAME)!!
        categoryName = x
        return x
    }

    private fun prepareRecyclerView() {
        myAdapter = MealRecyclerAdapter()
        binding.mealRecyclerview.apply {
            adapter = myAdapter
            layoutManager = GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
        }
    }
}
