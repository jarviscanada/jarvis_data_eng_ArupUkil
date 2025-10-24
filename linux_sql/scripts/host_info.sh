# Get args
psql_host=$1
psql_port=$2
db_name=$3
psql_user=$4
psql_password=$5

# Check number of args
if [ "$#" -ne 5 ]; then
  echo "Illegal number of parameters"
  exit 1
fi

# Save command to get specs to a variable
specs=`lscpu`

# Set host info and timestamp (UTC format) variables
cpu_number=$(echo "$specs" | egrep "^CPU\(s\):" | awk '{print $2}' | xargs)
cpu_architecture=$(echo "$specs" | egrep "^Architecture:" | awk '{print $2}' | xargs)
cpu_model=$(echo "$specs" | egrep "^Model name:" | cut -d: -f2- | xargs)
cpu_mhz=$(cat /proc/cpuinfo | egrep "^cpu MHz" | awk -F: '{print $2}' | tail -n1 | xargs)
l2_cache=$(echo "$specs" | egrep "^L2 cache:" | awk -F: '{print $2}' | awk '{print $1}' | xargs)
total_mem=$(cat /proc/meminfo | egrep "^MemTotal:" | awk '{print $2}' | xargs)
timestamp=$(vmstat -t | awk '{print $18 " " $19}' | tail -n1 | xargs)
hostname=$(hostname -f)

# Query to put server info data into host_info table
insert_stmt="INSERT INTO host_info(hostname, cpu_number, cpu_architecture, cpu_model, cpu_mhz, l2_cache, total_mem, \"timestamp\")
VALUES ('$hostname', $cpu_number, '$cpu_architecture', '$cpu_model', $cpu_mhz, $l2_cache, $total_mem, '$timestamp');"

# Set environment variable for psql command
export PGPASSWORD=$psql_password
# Insert data into the database
psql -h $psql_host -p $psql_port -d $db_name -U $psql_user -c "$insert_stmt"
exit $?