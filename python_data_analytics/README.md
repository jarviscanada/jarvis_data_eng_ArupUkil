# Introduction
LGS is an online gift and homeware retailer that wants to turn raw order history into decisions that improve revenue. The business already has a web application that captures retail transactions, but the transaction stream by itself does not explain how customers behave over time, when sales peak, how many orders are canceled, or which customer groups should be targeted differently. This project focuses on analyzing retail transactions so LGS can better understand order patterns, customer retention, and the characteristics of its highest-value shoppers.

The analytics produced here can support several business actions for LGS. Monthly sales and order trends help the business plan campaigns and staffing around strong and weak periods. Active-user and new-vs-existing-user views help the team judge whether revenue is being driven by acquisition or retention. RFM segmentation helps marketing prioritize who should receive loyalty offers, cross-sell campaigns, reactivation outreach, or discount-based win-back campaigns.

My work for this project was to load retail data from PostgreSQL and CSV sources into Pandas, clean and standardize the raw CSV schema, compute business-facing metrics in Jupyter Notebook, and visualize the results for interpretation. The solution uses Docker, PostgreSQL, Jupyter Notebook, Python, Pandas, NumPy, Matplotlib, Seaborn, and SQLAlchemy.

# Implementation
## Project Architecture
This project combines LGS transactional data, a PostgreSQL store, and a Jupyter-based analytics workflow. The LGS web application is the operational system that generates transaction records. Those records are stored in a PostgreSQL retail dataset that can be queried directly from the notebook. For data wrangling practice, the same dataset is also consumed as a CSV export and cleaned with Pandas before analysis. The notebook then calculates invoice distributions, monthly KPI trends, user lifecycle metrics, and RFM segments used for business recommendations.

```text
+------------------------+
|     LGS Web App       |
| Orders / Cancellations|
+-----------+------------+
            |
            | transaction data
            v
+------------------------+          +------------------------+
| PostgreSQL Retail DB   |          | CSV Export             |
| Docker: jarvis-psql    |          | online_retail_II.csv   |
+-----------+------------+          +-----------+------------+
            |                                   |
            | query via SQLAlchemy              | file-based ingestion
            +-------------------+---------------+
                                |
                                v
                    +--------------------------+
                    | Jupyter Notebook         |
                    | Python / Pandas / NumPy  |
                    | Matplotlib / Seaborn     |
                    +------------+-------------+
                                 |
                                 | wrangling + analytics
                                 v
                    +--------------------------+
                    | Business Insights        |
                    | Sales trends             |
                    | Order behavior           |
                    | Active users             |
                    | RFM segmentation         |
                    +------------+-------------+
                                 |
                                 | decisions
                                 v
                    +--------------------------+
                    | Marketing / Operations   |
                    | Retention campaigns      |
                    | Seasonal planning        |
                    | Inventory decisions      |
                    +--------------------------+
```

## Data Analytics and Wrangling
The completed notebook is here: [retail_data_analytics_wrangling.ipynb](./python_data_wrangling/retail_data_analytics_wrangling.ipynb)

The notebook covers two data-ingestion paths. First, it reads the `retail` table from PostgreSQL using SQLAlchemy and Pandas. Second, it loads the raw CSV export, renames columns into snake_case, converts dates and numeric fields into appropriate data types, and prepares a consistent analytics DataFrame. From there, the notebook computes invoice totals, monthly placed and canceled orders, monthly sales, sales growth, active users, new-vs-existing-user counts, and RFM metrics.

Several useful patterns came out of the analysis:
- Invoice values are highly skewed. The mean positive invoice amount is about `523.30`, while the median is `304.32`, the mode is `15.00`, and the maximum is `168,469.60`, which shows that a small number of very large invoices can distort average-based summaries.
- Monthly sales are seasonal. Sales peak strongly in late 2010, with November 2010 reaching about `1.47M` and December 2010 about `1.26M`, followed by a sharp drop in January 2011 of roughly `-45.24%`.
- User engagement also peaks in the holiday period. Monthly active users rise to `1,607` in November 2010, which lines up with the strongest sales month.
- Existing users drive more activity than new users after the initial months, which suggests retention quality matters as much as acquisition.
- RFM segmentation shows a large pool of `hibernating` customers (`1,535`) and strong premium groups such as `loyal_customers` (`1,169`) and `champions` (`835`). Champions have especially strong value, with average frequency around `23.72` and average monetary value around `10,668.99`.

These results can directly support revenue growth for LGS:
- `champions` and `loyal_customers` should receive early-access launches, premium bundles, and cross-sell recommendations because they already purchase frequently and spend the most.
- `potential_loyalists` and `new_customers` should receive onboarding promotions, follow-up recommendations, and second-purchase incentives to move them into higher-value segments.
- `at_risk` and `hibernating` customers should receive win-back campaigns, limited-time discounts, and reminders tied to categories they previously purchased from.
- Seasonal demand is strongest in the run-up to the holiday period, so LGS should concentrate campaign budget, inventory planning, and merchandising effort ahead of Q4 peaks.
- Because invoice amounts contain major outliers, LGS should monitor both median and mean invoice value when evaluating campaign performance.

# Improvements
- Add a reproducible ETL step that automatically loads the retail SQL dump and CSV into a dedicated analytics environment instead of relying on manual container setup.
- Extend the notebook into a lightweight dashboard so business users can filter by month, country, or segment without opening Jupyter.
- Improve the RFM section with more business-specific segmentation rules, product-category features, and validation from actual campaign outcomes.
