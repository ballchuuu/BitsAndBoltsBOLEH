//convert UTC time to local date time format
function convertUTCDateToLocalDate(date) {
    var newDate = new Date(date);
    newDate.setMinutes(date.getMinutes() - date.getTimezoneOffset());
    return newDate;
}

function memUsage() {
    $.getJSON('/api/getsysmon', function(data){
        time_x=[];
        cpu_y=[];
        mem_y=[];
        nwRx_y = [];
        nwTx_y = [];
        
        $.each(data, function(key, val){
            time_x.push(new Date(parseInt(val['timestamp']) * 1000));
            mem_y.push(val['mem_usage']);
            cpu_y.push(val['cpu_usage']);
            nwRx_y.push(val['network_rx']);
            nwTx_y.push(val['network_tx']);
        });
                
        var traceMem = {
          x: time_x,
          y: mem_y,
          mode: 'lines',
          name: 'Lines',
          marker: {color: 'rgb(75,137,218)'}
        };
        
        var traceCpu = {
          x: time_x,
          y: cpu_y,
          mode: 'lines',
          name: 'Lines',
          marker: {color: 'rgb(75,137,218)'}
        };
        
        var rx = {
          x: time_x,
          y: nwRx_y,
          mode: 'lines',
          name: 'Lines',
          marker: {color: 'rgb(75,137,218)'}
        };
        
        var tx = {
          x: time_x,
          y: nwTx_y,
          mode: 'lines',
          name: 'Lines',
          marker: {color: 'rgb(75,137,218)'}
        };
        
        var layout = {
          title:'Gateway CPU Utilization',
          font:{size:14},
          xaxis:{title:'Time of Day', titlefont:{size:16, color: '#646C77'}},
          yaxis:{title:'CPU Usage (%)', titlefont:{size:16, color: '#646C77'}}         
        };
        
        //Plot CPU
        dataset=[];
        dataset.push(traceCpu); //add to dataset
        Plotly.newPlot('cpuUsage', dataset, layout, {responsive:true});
        
        //Plot MEM
        dataset=[];
        layout['title'] = 'Gateway Memory Utilization';
        layout['yaxis'] = {title:'Mem Usage (%)', titlefont:{size:16, color: '#646C77'}}  
        
        dataset.push(traceMem); //add to dataset
        Plotly.newPlot('memUsage', dataset, layout, {responsive:true});

        //Plot RX
        dataset=[];
        layout['title'] = 'Gateway Network Rx';
        layout['yaxis'] = {title:'Network Rx', titlefont:{size:16, color: '#646C77'}}  
        
        dataset.push(rx); //add to dataset
        Plotly.newPlot('networkRx', dataset, layout, {responsive:true});
        
        //Plot TX
        dataset=[];
        layout['title'] = 'Gateway Network Tx';
        layout['yaxis'] = {title:'Network Tx', titlefont:{size:16, color: '#646C77'}}  
        
        dataset.push(tx); //add to dataset
        Plotly.newPlot('networkTx', dataset, layout, {responsive:true});

        
    });    
}



/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
$(document).ready(function(){
    
    memUsage();
            

});