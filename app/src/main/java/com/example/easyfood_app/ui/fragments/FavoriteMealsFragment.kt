package com.example.easyfood_app.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.easyfood_app.R
import com.example.easyfood_app.data.pojo.MealDB
import com.example.easyfood_app.databinding.FragmentFavoriteMealsBinding
import com.example.easyfood_app.extensions.Constants.Companion.CATEGORY_NAME
import com.example.easyfood_app.extensions.Constants.Companion.MEAL_AREA
import com.example.easyfood_app.extensions.Constants.Companion.MEAL_ID
import com.example.easyfood_app.extensions.Constants.Companion.MEAL_NAME
import com.example.easyfood_app.extensions.Constants.Companion.MEAL_STR
import com.example.easyfood_app.extensions.Constants.Companion.MEAL_THUMB
import com.example.easyfood_app.mvvm.DetailsMVVM
import com.example.easyfood_app.ui.MealBottomDialog
import com.example.easyfood_app.ui.activites.MealDetailsActivity
import com.example.easyfood_app.adapters.FavoriteMealsRecyclerAdapter
import com.google.android.material.snackbar.Snackbar

class FavoriteMeals : Fragment() {
    private lateinit var recView: RecyclerView
    private lateinit var fBinding: FragmentFavoriteMealsBinding
    private lateinit var myAdapter: FavoriteMealsRecyclerAdapter
    private lateinit var detailsMVVM: DetailsMVVM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        myAdapter = FavoriteMealsRecyclerAdapter()
        detailsMVVM = ViewModelProviders.of(this).get(DetailsMVVM::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fBinding = FragmentFavoriteMealsBinding.inflate(inflater, container, false)
        return fBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prepareRecyclerView(view)
        onFavoriteMealClick()
        onFavoriteLongMealClick()
        observeBottomDialog()

        detailsMVVM.observeSaveMeal().observe(viewLifecycleOwner, Observer { savedMeals ->
            myAdapter.setFavoriteMealsList(savedMeals)
            fBinding.tvFavEmpty.visibility = if (savedMeals.isEmpty()) View.VISIBLE else View.GONE
        })

        val itemTouchHelper = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val favoriteMeal = myAdapter.getMealByPosition(position)
                detailsMVVM.deleteMeal(favoriteMeal)
                showDeleteSnackBar(favoriteMeal)
            }
        }

        ItemTouchHelper(itemTouchHelper).attachToRecyclerView(recView)
    }

    private fun showDeleteSnackBar(favoriteMeal: MealDB) {
        Snackbar.make(requireView(), "Meal was deleted", Snackbar.LENGTH_LONG).apply {
            setAction("undo") {
                detailsMVVM.insertMeal(favoriteMeal)
            }
            show()
        }
    }

    private fun observeBottomDialog() {
        detailsMVVM.observeMealBottomSheet().observe(viewLifecycleOwner, Observer { mealDetails ->
            val bottomDialog = MealBottomDialog()
            val bundle = Bundle().apply {
                putString(CATEGORY_NAME, mealDetails[0].strCategory)
                putString(MEAL_AREA, mealDetails[0].strArea)
                putString(MEAL_NAME, mealDetails[0].strMeal)
                putString(MEAL_THUMB, mealDetails[0].strMealThumb)
                putString(MEAL_ID, mealDetails[0].idMeal)
            }
            bottomDialog.arguments = bundle
            bottomDialog.show(childFragmentManager, "Favorite bottom dialog")
        })
    }

    private fun prepareRecyclerView(view: View) {
        recView = view.findViewById(R.id.fav_rec_view)
        recView.adapter = myAdapter
        recView.layoutManager = GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
    }

    private fun onFavoriteMealClick() {
        myAdapter.setOnFavoriteMealClickListener(object : FavoriteMealsRecyclerAdapter.OnFavoriteClickListener {
            override fun onFavoriteClick(meal: MealDB) {
                val intent = Intent(context, MealDetailsActivity::class.java).apply {
                    putExtra(MEAL_ID, meal.mealId.toString())
                    putExtra(MEAL_STR, meal.mealName)
                    putExtra(MEAL_THUMB, meal.mealThumb)
                }
                startActivity(intent)
            }
        })
    }

    private fun onFavoriteLongMealClick() {
        myAdapter.setOnFavoriteLongClickListener(object : FavoriteMealsRecyclerAdapter.OnFavoriteLongClickListener {
            override fun onFavoriteLongCLick(meal: MealDB) {
                detailsMVVM.getMealByIdBottomSheet(meal.mealId.toString())
            }

            override fun onFavoriteLongClick(meal: MealDB) {
                detailsMVVM.getMealByIdBottomSheet(meal.mealId.toString())
            }
        })
    }
}
