/*
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
var showControllersOnly = false;
var seriesFilter = "";
var filtersOnlySampleSeries = true;

/*
 * Add header in statistics table to group metrics by category
 * format
 *
 */
function summaryTableHeader(header) {
    var newRow = header.insertRow(-1);
    newRow.className = "tablesorter-no-sort";
    var cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 1;
    cell.innerHTML = "Requests";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 3;
    cell.innerHTML = "Executions";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 7;
    cell.innerHTML = "Response Times (ms)";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 1;
    cell.innerHTML = "Throughput";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 2;
    cell.innerHTML = "Network (KB/sec)";
    newRow.appendChild(cell);
}

/*
 * Populates the table identified by id parameter with the specified data and
 * format
 *
 */
function createTable(table, info, formatter, defaultSorts, seriesIndex, headerCreator) {
    var tableRef = table[0];

    // Create header and populate it with data.titles array
    var header = tableRef.createTHead();

    // Call callback is available
    if(headerCreator) {
        headerCreator(header);
    }

    var newRow = header.insertRow(-1);
    for (var index = 0; index < info.titles.length; index++) {
        var cell = document.createElement('th');
        cell.innerHTML = info.titles[index];
        newRow.appendChild(cell);
    }

    var tBody;

    // Create overall body if defined
    if(info.overall){
        tBody = document.createElement('tbody');
        tBody.className = "tablesorter-no-sort";
        tableRef.appendChild(tBody);
        var newRow = tBody.insertRow(-1);
        var data = info.overall.data;
        for(var index=0;index < data.length; index++){
            var cell = newRow.insertCell(-1);
            cell.innerHTML = formatter ? formatter(index, data[index]): data[index];
        }
    }

    // Create regular body
    tBody = document.createElement('tbody');
    tableRef.appendChild(tBody);

    var regexp;
    if(seriesFilter) {
        regexp = new RegExp(seriesFilter, 'i');
    }
    // Populate body with data.items array
    for(var index=0; index < info.items.length; index++){
        var item = info.items[index];
        if((!regexp || filtersOnlySampleSeries && !info.supportsControllersDiscrimination || regexp.test(item.data[seriesIndex]))
                &&
                (!showControllersOnly || !info.supportsControllersDiscrimination || item.isController)){
            if(item.data.length > 0) {
                var newRow = tBody.insertRow(-1);
                for(var col=0; col < item.data.length; col++){
                    var cell = newRow.insertCell(-1);
                    cell.innerHTML = formatter ? formatter(col, item.data[col]) : item.data[col];
                }
            }
        }
    }

    // Add support of columns sort
    table.tablesorter({sortList : defaultSorts});
}

$(document).ready(function() {

    // Customize table sorter default options
    $.extend( $.tablesorter.defaults, {
        theme: 'blue',
        cssInfoBlock: "tablesorter-no-sort",
        widthFixed: true,
        widgets: ['zebra']
    });

    var data = {"OkPercent": 100.0, "KoPercent": 0.0};
    var dataset = [
        {
            "label" : "FAIL",
            "data" : data.KoPercent,
            "color" : "#FF6347"
        },
        {
            "label" : "PASS",
            "data" : data.OkPercent,
            "color" : "#9ACD32"
        }];
    $.plot($("#flot-requests-summary"), dataset, {
        series : {
            pie : {
                show : true,
                radius : 1,
                label : {
                    show : true,
                    radius : 3 / 4,
                    formatter : function(label, series) {
                        return '<div style="font-size:8pt;text-align:center;padding:2px;color:white;">'
                            + label
                            + '<br/>'
                            + Math.round10(series.percent, -2)
                            + '%</div>';
                    },
                    background : {
                        opacity : 0.5,
                        color : '#000'
                    }
                }
            }
        },
        legend : {
            show : true
        }
    });

    // Creates APDEX table
    createTable($("#apdexTable"), {"supportsControllersDiscrimination": true, "overall": {"data": [0.22896825396825396, 500, 1500, "Total"], "isController": false}, "titles": ["Apdex", "T (Toleration threshold)", "F (Frustration threshold)", "Label"], "items": [{"data": [0.6966666666666667, 500, 1500, "登录"], "isController": false}, {"data": [0.171, 500, 1500, "发布评论"], "isController": false}, {"data": [0.118, 500, 1500, "收藏"], "isController": false}, {"data": [0.193, 500, 1500, "帖子详情"], "isController": false}, {"data": [0.29333333333333333, 500, 1500, "注册(已存在则忽略)"], "isController": false}, {"data": [0.102, 500, 1500, "评论列表"], "isController": false}, {"data": [0.117, 500, 1500, "点赞"], "isController": false}, {"data": [0.0485, 500, 1500, "帖子列表"], "isController": false}, {"data": [0.222, 500, 1500, "关注"], "isController": false}, {"data": [0.6365, 500, 1500, "通知未读数"], "isController": false}]}, function(index, item){
        switch(index){
            case 0:
                item = item.toFixed(3);
                break;
            case 1:
            case 2:
                item = formatDuration(item);
                break;
        }
        return item;
    }, [[0, 0]], 3);

    // Create statistics table
    createTable($("#statisticsTable"), {"supportsControllersDiscrimination": true, "overall": {"data": ["Total", 6300, 0, 0.0, 2168.0020634920575, 11, 8004, 1934.0, 4082.800000000001, 4880.749999999999, 6084.9299999999985, 60.761544693491764, 133.55149608726998, 18.547953170812278], "isController": false}, "titles": ["Label", "#Samples", "FAIL", "Error %", "Average", "Min", "Max", "Median", "90th pct", "95th pct", "99th pct", "Transactions/s", "Received", "Sent"], "items": [{"data": ["登录", 150, 0, 0.0, 591.04, 19, 1636, 624.5, 1004.9, 1230.5, 1530.4300000000019, 11.203226529240421, 5.639249103741878, 2.9345013863992833], "isController": false}, {"data": ["发布评论", 500, 0, 0.0, 1988.296, 138, 4708, 1983.5, 3084.4000000000005, 3341.35, 3965.51, 5.354465624330691, 2.7197234251177984, 1.8279852819126152], "isController": false}, {"data": ["收藏", 500, 0, 0.0, 2152.6080000000006, 366, 7558, 2124.0, 3143.2000000000007, 3593.25, 5289.130000000004, 5.27198152697673, 1.2736880838719542, 1.6666360507322784], "isController": false}, {"data": ["帖子详情", 1000, 0, 0.0, 2927.6450000000004, 31, 8004, 2134.0, 5712.099999999999, 6274.849999999999, 7050.1900000000005, 9.8294588882882, 7.932219737553448, 2.793332555167838], "isController": false}, {"data": ["注册(已存在则忽略)", 150, 0, 0.0, 1635.239999999999, 95, 4078, 1697.0, 2833.9000000000005, 3065.45, 3983.1400000000017, 11.209924519841566, 2.3645934534040802, 3.3546574900979], "isController": false}, {"data": ["评论列表", 1000, 0, 0.0, 2514.917, 86, 5009, 2665.0, 3610.7, 3838.7499999999995, 4425.87, 9.975261351847418, 32.323041980887396, 2.922439849174048], "isController": false}, {"data": ["点赞", 500, 0, 0.0, 2108.019999999999, 340, 5066, 2054.0, 3106.6000000000004, 3460.45, 4416.740000000002, 5.369358146927117, 1.260519747156925, 1.771091174386014], "isController": false}, {"data": ["帖子列表", 1000, 0, 0.0, 3251.308000000001, 94, 5983, 3394.0, 4520.7, 4874.399999999998, 5604.610000000001, 9.746588693957115, 86.5568557352583, 3.0648452729044835], "isController": false}, {"data": ["关注", 500, 0, 0.0, 1613.39, 257, 4310, 1593.0, 2313.5, 2545.7499999999995, 3056.6900000000005, 5.211318984835062, 1.1654219214133097, 1.5831926005784565], "isController": false}, {"data": ["通知未读数", 1000, 0, 0.0, 699.4439999999993, 11, 2425, 706.0, 1169.2999999999997, 1288.9499999999998, 1750.6300000000003, 10.120638005019837, 2.10517177252854, 3.0539815854991494], "isController": false}]}, function(index, item){
        switch(index){
            // Errors pct
            case 3:
                item = item.toFixed(2) + '%';
                break;
            // Mean
            case 4:
            // Mean
            case 7:
            // Median
            case 8:
            // Percentile 1
            case 9:
            // Percentile 2
            case 10:
            // Percentile 3
            case 11:
            // Throughput
            case 12:
            // Kbytes/s
            case 13:
            // Sent Kbytes/s
                item = item.toFixed(2);
                break;
        }
        return item;
    }, [[0, 0]], 0, summaryTableHeader);

    // Create error table
    createTable($("#errorsTable"), {"supportsControllersDiscrimination": false, "titles": ["Type of error", "Number of errors", "% in errors", "% in all samples"], "items": []}, function(index, item){
        switch(index){
            case 2:
            case 3:
                item = item.toFixed(2) + '%';
                break;
        }
        return item;
    }, [[1, 1]]);

        // Create top5 errors by sampler
    createTable($("#top5ErrorsBySamplerTable"), {"supportsControllersDiscrimination": false, "overall": {"data": ["Total", 6300, 0, "", "", "", "", "", "", "", "", "", ""], "isController": false}, "titles": ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"], "items": [{"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}]}, function(index, item){
        return item;
    }, [[0, 0]], 0);

});
