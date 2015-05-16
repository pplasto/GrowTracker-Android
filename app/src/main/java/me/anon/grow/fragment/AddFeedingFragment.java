package me.anon.grow.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.ArrayList;

import me.anon.grow.R;
import me.anon.lib.Views;
import me.anon.lib.manager.PlantManager;
import me.anon.model.Action;
import me.anon.model.Feed;
import me.anon.model.Nutrient;
import me.anon.model.Plant;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
@Views.Injectable
public class AddFeedingFragment extends Fragment
{
	@Views.InjectView(R.id.water_ph) private TextView waterPh;
	@Views.InjectView(R.id.water_ppm) private TextView waterPpm;
	@Views.InjectView(R.id.runoff_ph) private TextView runoffPh;
	@Views.InjectView(R.id.amount) private TextView amount;
	@Views.InjectView(R.id.nutrient_container) private View nutrientContainer;
	@Views.InjectView(R.id.nutrient) private TextView nutrient;
	@Views.InjectView(R.id.nutrient_amount) private TextView nutrientAmount;

	private int plantIndex = -1;
	private Plant plant;
	private Feed feed;

	/**
	 * @param plantIndex If -1, assume new plant
	 * @return Instantiated details fragment
	 */
	public static AddFeedingFragment newInstance(int plantIndex, boolean feeding)
	{
		Bundle args = new Bundle();
		args.putInt("plant_index", plantIndex);
		args.putBoolean("feeding", feeding);

		AddFeedingFragment fragment = new AddFeedingFragment();
		fragment.setArguments(args);

		return fragment;
	}

	@Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.add_feeding_view, container, false);
		Views.inject(this, view);

		return view;
	}

	@Override public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		getActivity().setTitle("New feeding");

		if (getArguments() != null)
		{
			plantIndex = getArguments().getInt("plant_index");

			if (plantIndex > -1)
			{
				plant = PlantManager.getInstance().getPlants().get(plantIndex);
				getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			}

			boolean feeding = getArguments().getBoolean("feeding", true);

			if (!feeding)
			{
				nutrientContainer.setVisibility(View.GONE);
			}
		}

		feed = new Feed();

		if (plant == null)
		{
			getActivity().finish();
			return;
		}

		nutrient.setOnFocusChangeListener(new View.OnFocusChangeListener()
		{
			@Override public void onFocusChange(View v, boolean hasFocus)
			{
				if (hasFocus)
				{
					Nutrient nutrient = feed.getNutrient();
					if (feed.getNutrient() == null)
					{
						if (plant.getActions() != null)
						{
							ArrayList<Action> actions = plant.getActions();
							for (int i = actions.size() - 1; i >= 0; i--)
							{
								Action action = actions.get(i);
								if (action instanceof Feed)
								{
									nutrient = nutrient;
									break;
								}
							}
						}
					}

					FragmentManager fm = getFragmentManager();
					AddNutrientDialogFragment addNutrientDialogFragment = new AddNutrientDialogFragment(nutrient);
					addNutrientDialogFragment.setOnAddNutrientListener(new AddNutrientDialogFragment.OnAddNutrientListener()
					{
						@Override public void onNutrientSelected(Nutrient nutrient)
						{
							feed.setNutrient(nutrient);

							String nutrientStr = "";
							nutrientStr += nutrient.getNpc() == null ? "-" : nutrient.getNpc();
							nutrientStr += " : ";
							nutrientStr += nutrient.getPpc() == null ? "-" : nutrient.getPpc();
							nutrientStr += " : ";
							nutrientStr += nutrient.getKpc() == null ? "-" : nutrient.getKpc();
							nutrientStr += "/";
							nutrientStr += nutrient.getCapc() == null ? "-" : nutrient.getCapc();
							nutrientStr += " : ";
							nutrientStr += nutrient.getSpc() == null ? "-" : nutrient.getSpc();
							nutrientStr += " : ";
							nutrientStr += nutrient.getMgpc() == null ? "-" : nutrient.getMgpc();

							AddFeedingFragment.this.nutrient.setText(nutrientStr);
						}
					});
					addNutrientDialogFragment.show(fm, "fragment_add_nutrient");
				}
			}
		});
	}

	@Views.OnClick public void onFabCompleteClick(final View view)
	{
		Double waterPh = TextUtils.isEmpty(this.waterPh.getText()) ? null : Double.valueOf(this.waterPh.getText().toString());
		Long ppm = TextUtils.isEmpty(this.waterPpm.getText()) ? null : Long.valueOf(this.waterPpm.getText().toString());
		Double runoffPh = TextUtils.isEmpty(this.runoffPh.getText()) ? null : Double.valueOf(this.runoffPh.getText().toString());
		Integer amount = TextUtils.isEmpty(this.amount.getText()) ? null : Integer.valueOf(this.amount.getText().toString());
		Integer nutrientAmount = TextUtils.isEmpty(this.nutrientAmount.getText()) ? null : Integer.valueOf(this.nutrientAmount.getText().toString());

		feed.setPh(waterPh);
		feed.setPpm(ppm);
		feed.setRunoff(runoffPh);
		feed.setAmount(amount);
		feed.setMlpl(nutrientAmount);

		if (plant.getActions() == null)
		{
			plant.setActions(new ArrayList<Action>());
		}

		plant.getActions().add(feed);
		PlantManager.getInstance().upsert(plantIndex, plant);
		getActivity().finish();
	}
}