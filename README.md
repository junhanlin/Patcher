# README
> For artifacts evaluation, we enclose all the needed data and executable in a single VM image named `issta.ova` which can be opened by virtualization software VirtualBox. Here's the login information of the VM:
> **User Name: issta**
> **Password: ae**
> Beside the VM, we also prepared a response file called **ISSTA_AE_RESPONSE.docx** for reviewers.



[TOC]

## Artifact File Structure
Once you unzip our archive file, you should see the fallowing file structure:
```
$ROOT
|-- patch-synthesis
|	|-- Patcher
|	|   |-- src
|	|   |-- target
|	|   |-- pom.xml
|	|-- PatcherController
|	|	|-- ...
|	|-- PatcherWorker
|	|	|-- ...
|	|-- benchmark-src-code
|	|   |-- benchmarks.zip
|	|   |-- market.zip
|	|	|-- moodle1_6.zip
|	|	|-- nucleus3.64.zip
|	|	|-- PBLGuestbook.zip
|	|	|-- php-fusion-6-01-18.zip
|	|	|-- schoolmate.zip
|	|	|-- sendcard_3-4-1.zip
|	|	|-- servoo.zip
|	|	|-- examples.zip
|	|	|-- e107.zip
|	|-- benchmark-analysis-results
|		|-- vuln-script-depGraph-signature
|		|   |-- benchmarks
|		|   |-- market
|		|	|-- moodle1_6
|		|	|-- nucleus3.64
|		|	|-- PBLGuestbook
|		|	|-- php-fusion-6-01-18
|		|	|-- schoolmate
|		|	|-- sendcard_3-4-1
|		|	|-- servoo
|		|	|-- examples
|		|	|-- e107
|		|-- benchmark-app-patches
|		    |-- optPatch183.zip
|		    |-- optPatch186.zip
|			|-- optPatch188.zip
|			|-- optPatch189.zip
|			|-- optPatch190.zip
|			|-- optPatch<project-id>.zip
|			|-- ...
|-- performance-evaluation
|   |-- OptimalPatch
|		|-- src
|	   	|	|-- main
|		|	|	|-- java
|		|   |   |-- resources
|	   	|   |-- test
|		|	|	|-- java
|		|	|	|-- resources
|		|	|		|-- edu.nccu.soslab.optpatch.comalg
|		|	|		|-- edu.nccu.soslab.optpatch.typopatch
|		|	|        
|		|-- script
|		|	|-- build
|		|   |-- optpatch
|	   	|	|-- run-test-CompareAlgorithms
|		|   |-- run-test-TypoPatch
|	   	|-- .gitignore
|	   	|-- pom.xml
```

In this `README` file we will use `$ROOT` to represent the the top level directory  of the whole artifact. In the VM, `$ROOT` is actually located at **/home/issta/artifact**. 

## Patch Synthesis (Table 1)
To reproduce data in **Table 1**, please do the fallowing steps to start the Patcher Website inside the VM:

### Step 1. Start the Patcher Website
Open a new terminal in the VM and input fallowing command lines
```
$ cd /home/issta/artifact/patch-synthesis/Patcher
$ mvn tomcat7:run
```
### Step 2. Start the Patcher Controller
Patcher Controller is the job tracker of each submitted analysis task on the patcher website, you can open a new terminal and input the fallowing command lines on start it.
```
$ cd /home/issta/artifact/patch-synthesis/PatcherController
$ ./script/patcher-ctrl
```

### Step 3. Upload benchmarks to Patcher Website
Open the FireFox browser provided in the VM and browse to the fallowing URL to access the Patcher Website:
```
http://localhost:9090/
```
The benchmarks PHP applications are provided under:
```
/home/issta/artifact/patch-synthesis/benchmark-src-code
```
You can upload those **\*.zip** file using Patcher Website by the **guest** account.
During the upload process, you should be prompt to input **attack patterns** for each of the attack type such as XSS, SQL-Injection and MFE, and here's the attack patterns we used for each attack type:

**SQL-Injection:**
```
/.*' or 1=1 '.*/
/.*((\%27)|(\'))\s*((\%6F)|o|(\%4F))\s*((\%72)|r|(\%52))\s*.*/
/.*((\%27)|(\'))union.*/
/.*exec(\s|\+)+(s|x)p.*/

```
**XSS**
```
/.*\<SCRIPT .*\>.*/

/.*javascript:.*/

/.*[j|J][a|A][v|V][a|A][s|S][c|C][r|R][i|I][p|P][t|T]:.*/

/.*\\\";.*\/\/.*/

/.*vbscript:.*/                
                                                                                                                                                  /.*livescript:.*/
                                                                                                                                                  

/.*PHNjcmlwdD5hbGVydCgnWFNTJyk8L3NjcmlwdD4K.*/

/.*\\0075\\0072\\006C\\0028'\\006a\\0061\\0076\\0061\\0073\\0063\\0072\\0069\\0070\\0074\\003a\\0061\\006c\\0065\\0072\\0074\\0028\.1027\\0058\.1053\\0053\\0027\\0029'\\0029.*/
```
**MFE** 
```
/.*/evil.*/
```

> **NOTICE:** Since current version of Patcher Website only support one attack pattern for each attack type per upload, if you intends to run all the attack pattern for a single benchmark, you must upload multiple times.

### Step 4. Wait for the analysis to complete
Since it takes times to complete the analysis process, the benchmarks might not be analyzed immediately once they are uploaded. Instead, you should click one of the **"SQLI"**, **"XSS"**, **"MFE"** button on the project list and check the **pie chart** to confirm the analysis process. If the pie chart says some instances are **"Pending"**, then it means that the instance is still waiting to be processed.

### Step 5. Download the result of Table 1
Once all the uploaded benchmarks have completed the analysis process, you can download the LaTex format table by clicking **Download LaTex Table Summary** button in the project list page of Patcher Website.

Also, you can click **Download All Vulnerable Code & Signature** to downlaod the **sanitization signature**, **dependency graph**, **attack pattern** files. (Also see [Expected Analysis Result](#expected-analysis-result))

To download the **patch**, you can click **Patch** button on the project list page. This will download a `zip` file that contains some sanitization signatures and a guiding `README` file on how to patch those vulnerability.  (Also see [Expected Analysis Result](#expected-analysis-result))


### Expected Analysis Result
We also prepare expected analysis result for those benchmarks and they are placed under **/home/issta/artifact/benchmark-analysis-results**. Here's the guide to read the result:

#### Vulnerable PHP Files, Dependency Graphs, Attack Patterns and Signatures

We put **9** benchmarks which act as our experiment benchmark in the paper under  **$ROOT/benchmark-analysis-results/vuln-script-depGraph-signature** .  In this directory, we intend to provide you the analysis results of each individual vulnerable sinks that found in the `php` file.

In each application folder, we include all php files that have **vulnerabilities**.
Each `php` file is placed under the path that is the same as their original path in their application with a duplicated directory.

E.g., a php file's original path is:

```
market/basket.php
```
then in the folder its path becomes:

```
$ROOT/patch-synthesis/benchmark-analysis-results/vuln-script-depGraph-signature/market/basket.php/basket.php
```

The additional folder, i.e., **$ROOT/patch-synthesis/benchmark-analysis-results/vuln-script-depGraph-signature/market/basket.php/**, also includes the analysis results of this php file. We separate the results in `sink-folders` for each kind of vulnerability:

```
1. XSS
2. SQLI
3. MFE
```
Inside each `sink-folder`, we include vulnerabilities of the `php` file. For each vulnerability, we have

1. **depGraph.dot** : The `dependency graph` of the sink node that is vulnerable (in the `.dot` format that can be opened via [Graphviz](http://www.graphviz.org/))
2. **sink.info** : The metadata of this vulnerability includes: the **sink node**, the **attack pattern** (the sink node is vulnerable with respect to this attack pattern), the **number of input nodes**,  the **analysis time**, and the **signature information**.
3. **white.auto.dot** : The `sanitization signature` that is generated to patch the vulnerability.

> **NOTICE:** Since we only include the `php` file which have **vulnerabilities**, not all `php` files in the original application source code will be shown. In other words, php files that contains no **tainted sink** or `php` files that has tainted sink but are consider as **not vulnerable** by forward analysis will not be shown.

#### Patches
We put **78** *.zip files under  **$ROOT/benchmark-analysis-results/benchmark-app-patches** which are the patch files of each benchmarks. After unzip those files, you can see  
sanitization signatures and a guiding `README` file on how to patch those vulnerabilities.

> **NOTICE:** The 78 \*.zip files is actually downloaded from patcher website. And since current version of patcher website only support inputing at most one attack pattern for each single attack type, we had to upload multiple times for a single benchmarks in order to test different attack pattern on same benchmark. And thats why the number of `zip` files under **ROOT/benchmark-analysis-results/benchmark-app-patches**  is more them the number of .zip files under **ROOT/patch-synthesis/benchmark-src-code**

## Evaluation

### Performance Evaluation (Figures 5, 6)
To reproduce **figures 5 & 6**, please do the fallowing steps:

#### Step 1. Prepare sanitization signatures (Optional)
We've already prepared the **3** sanitization signatures (the state size are 12, 61, and 73) in *.dot format at:
```
/home/issta/artifact/performance-evaluation/OptimalPatch/src/test/resources/edu/nccu/soslab/optpatch/comalg
```
If you intend to use other sanitization signature which downloaded from Patcher Website, you can also placed them into this directory too.

#### Step 2. Run performance evaluation
Open a new terminal in the VM and run the fallowing command:
```
$ cd /home/issta/artifact/performance-evaluation/OptimalPatch
$ ./script/run-test-CompareAlgorithms
```
this command will randomly generate 50 string (length form 1 to 50) and use the sanitization signauture in step 1 the generate optimal patch with each of the fallowing three algorithms:
```
1. precipitation
2. incremenatal
3. co-graph
```
> **NOTICE:** The command might take longer time to finish since co-graph algorithm generally take longer time to do the patch.

#### Step 3. Get the result CSV file
The performance output will be reported to CSV files which has the same name as *.dot file in step 1 but has "**.csv**" extension. The output CSV file is under:
```
/home/issta/artifact/performance-evaluation/OptimalPatch/target/test-classes/edu/nccu/soslab/optpatch/comalg
```


### Sanitization Evaluation (Table 3)
In table 3, we split the sanitization evaluation into 3 parts:

1. **Part1** - use complement of some attack pattern to patch
2. **Part2** - use desired pattern to path
3. **Part3** - use a automata that is the intersection of complemented attack pattern and a desired pattern

#### Reproduce Part 1

##### Step 1. Check the input files
The attack patterns (A1~A6) are placed in the input file :
```
/home/issta/artifact/performance-evaluation/OptimalPatch/src/test/resources/edu/nccu/soslab/optpatch/typopatch/attack-patterns.txt
```
The input strings that will be used are in the input file:
```
/home/issta/artifact/performance-evaluation/OptimalPatch/src/test/resources/edu/nccu/soslab/optpatch/typopatch/input-a.txt
```

##### Step 2. Run the patch command
Please run the fallowing command lines:
```
$ cd /home/issta/artifact/performance-evaluation/OptimalPatch
$ ./script/run-test-TypoPatch-A
```

##### Step 3. Checkout the output
The output result is in this file (plain text format):
```
/home/issta/artifact/performance-evaluation/OptimalPatch/target/surefire-reports/edu.nccu.soslab.optpatch.typopatch.TestTypoPatchA-output.txt
```


#### Reproduce Part 2

##### Step 1. Check the input files
The desired patterns (I1~I10) are placed in the input file :
```
/home/issta/artifact/performance-evaluation/OptimalPatch/src/test/resources/edu/nccu/soslab/optpatch/typopatch/desired-patterns.txt
```
The input strings that will be used are in the input file:
```
/home/issta/artifact/performance-evaluation/OptimalPatch/src/test/resources/edu/nccu/soslab/optpatch/typopatch/input-i.txt
```

##### Step 2. Run the patch command
Please run the fallowing command lines:
```
$ cd /home/issta/artifact/performance-evaluation/OptimalPatch
$ ./script/run-test-TypoPatch-I
```

##### Step 3. Checkout the output
The output result is in this file (plain text format):
```
/home/issta/artifact/performance-evaluation/OptimalPatch/target/surefire-reports/edu.nccu.soslab.optpatch.typopatch.TestTypoPatchI-output.txt
```

#### Reproduce Part 3

##### Step 1. Check the input files
The input strings and the pairs of attack pattern and desired pattern that will be used are in the input file:
```
/home/issta/artifact/performance-evaluation/OptimalPatch/src/test/resources/edu/nccu/soslab/optpatch/typopatch/input-a-i.txt
```

##### Step 2. Run the patch command
Please run the fallowing command lines:
```
$ cd /home/issta/artifact/performance-evaluation/OptimalPatch
$ ./script/run-test-TypoPatch-AI
```

##### Step 3. Checkout the output
The output result is in this file (plain text format):
```
/home/issta/artifact/performance-evaluation/OptimalPatch/target/surefire-reports/edu.nccu.soslab.optpatch.typopatch.TestTypoPatchAI-output.txt
```
