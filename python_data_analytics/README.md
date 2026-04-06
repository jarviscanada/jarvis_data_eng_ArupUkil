# Introduction
This project focuses on analyzing retail transactions from the company London Gift Shop (LGS) so that LGS can better understand order patterns, customer retention, and the characteristics of its highest-value shoppers. LGS is an online gift and homeware retailer that wants to turn raw order history into decisions that improve revenue hence they commissioned Jarvis Consulting to a proof-of-concept (PoC) analytics solution. The business already has a web application that captures retail transactions, but the transaction data by itself does not explain how customers behave over time, when sales peak, how many orders are canceled, or which customer groups should be targeted differently.

My work for this project was to load retail data from CSV sources (though can be easily switched to PostgreSQL databases) into Pandas, clean and standardize the raw CSV schema, compute business-facing metrics in Jupyter Notebook, and visualize the results for interpretation. The solution uses Docker, PostgreSQL, Jupyter Notebook, Python, Pandas, NumPy, Matplotlib, Seaborn, and SQLAlchemy.

# Implementation
## Project Architecture
The LGS web application is the OLAP (Online Analytical Processing) system that generates transaction records and stores them in a database. Those records can be used in a Jupyter notebook with a connection to the PostgreSQL database or CSV export of it. For the data wrangling, the data is cleaned with Pandas before analysis and then the notebook calculates invoice distributions, monthly KPI trends, user lifecycle metrics, and RFM segments used for business recommendations. That data is then used for business decisions for LGS.

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
| Docker: jarvis-psql    |    OR    | online_retail_II.csv   |
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

The notebook has potentially two data-ingestion paths. It can read the `retail` table from PostgreSQL using SQLAlchemy and Pandas as shown in the initial section but for the actual analysis, it loads the raw CSV export and then cleans up the data to prepare a consistent analytics DataFrame. From there, the notebook computes invoice totals, monthly placed and canceled orders, monthly sales, sales growth, active users, new-vs-existing-user counts, and RFM metrics.

Several useful data came out of the analysis:
- Seasonal demand is strongest in the run-up to the holiday period, so LGS should concentrate campaign budget, inventory planning, and merchandising effort ahead of Q4 peaks.
- Existing users drive more activity than new users after the initial months, which suggests retention quality matters as much as acquisition.
- Cancellation leakage is large enough to justify operational instrumentation, with $1.53M of potential lost value through the two years looked at.

# Improvements
- Add a reproducible ETL step that automatically loads the retail SQL dump and CSV into a dedicated analytics environment instead of relying on manual container setup.
- Extend the notebook into a lightweight dashboard so business users can filter by month, country, or segment without opening Jupyter.
- Improve the RFM section with more business-specific segmentation rules, product-category features, and validation from actual campaign outcomes.
