# Jupyter Notebooks
These Jupyter Notebooks can be used to process the recorded data.
The raw data is in CSV Format, which can be exported by wireshark. Wireshark can be used to open PCAP files or the Bluetooth HCI Log Files.

## Structure
### Part 1
Read all CSV Files in folder "measurements" and subfolders and create CSV with Request Informations (HCI_Output.CSV/OTA_Output.CSV)
- HCI-paket-analyser.ipynb => read all csv in this folder and subfolders and create HCI_Output.CSV File
- ubertooth-paket-analyser.ipynb => read all csv in this folder and subfolders and create OTA_Output.CSV File
- UbertoothHelpClasses.ipynb => Provides a Request Class, which is used by the two paket-analyser notebooks
### Part 2
Read HCI_Output.CSV/OTA_Output.CSV to display values
- Compare Generally.ipynb => Display Information, where all values can be compared
- Read Analyse.ipynb => Display Information, where only Read-Requests can be compared (based on HCI Data)
- Notify Analyse.ipynb => Display Information, where only Notify-Requests can be compared (based on HCI Data)

## Detail
### Part 1
In folder "measurements" are csv files exported by wireshark.  
Each row represents a Paket or Instruction in the HCI.  
To enhance the overview those are filtered on ATT-Pakets only. Other Pakets are not needed.  
Each paket is identified to be one of the following types:
- Read Request
- Read Response
- Read Blob Request
- Read Blob Response
- Write Command - Connection-Interval
- Write Command - Data Size
- Write Response - Connection-Interval
- Write Resposne - Data Size
- MTU Exchange Request
- MTU Exchange Response
- Write Command - Send Data from Server to Client
- Notification Paket 

#### Group Requests
Those pakets can be grouped to a request. Each Read-Request has to start with Read-Request and can End with Read Response or Read Blob Response. Due to missing pakets, this was quite difficult. This problem occured only on ubertooth traces. HCI was fine.  
Thats why a Regex was created, which tries to Group the pakets accordingly.

The same was done for Notification pakets, which was a little bit easier, because they start with Write Command and only can contain Notification Pakets.

With this technique pakets could be grouped to requests.
#### Determine MTU / Data / Connection-Interval
The timestamps of Write Command - Data Size and Connection-Interval and the MTU Exchange Request could be used to create "breakpoints" where the properties changed. Those pakets contain Information about the new Value and the timestamp.  
A List with Property changes and Time is the result.  
The first Paket from every Request can be compared with the latest properites change for each of the properties. The Requests MTU/Data/Connection-Interval properties can be updated and the request contains all information needed.

#### Validate & Export
Now every Request can be checked about number of received Pakets and expected Pakets. Expectation results from the Formular, which is presented in the Bachelorthesis.  
Only valid Requests will be used and the following information will be exported into a CSV (HCI_Output.CSV/OTA_Output.CSV).
- Type (Read / Notify)
- First Paket Timestamp (Start of transfer)
- Last Paket Timestamp (End of transfer)
- Duration (Difference between start and end)
- Transfered Data Size
- Used MTU
- Used Connection-Interval
- Amount of Read Request Pakets
- Amount of Read Responses
- Amount of Notification Pakets
- Data (which is malformed unfortunately)

### Part 2
The provided Request Data can be used to compare.  
Therefore different properties can be grouped.  
In the most cases, two of the properties are grouped and changes in the last one are compared.  


### Start
To create a Docker-Container use the following command:
```sudo docker run --name=blethesis -v $(pwd)/.:/home/jovyan/work -p 8888:8888 jupyter/datascience-notebook```
For later Use:
```sudo docker start blethesis -a```

