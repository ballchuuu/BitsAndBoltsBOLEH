var socket = io.connect('http://' + document.domain + ':' + location.port);
socket.on('updates', function (data) {
  var trace1 = {
    x: data.x,
    y: data.y,
    mode: 'markers',
    marker: {
      size: data.count
    },
    type: 'scatter'
  };

  Plotly.plot('plotly-div', [trace1], layout);
});

function plotFloor() {
  var layout = {
    autosize: false,
    images: [
      {
        source: 'https://raw.githubusercontent.com/ballchuuu/BitsAndBoltsBOLEH/master/layouts/COM1_L2_2.jpg?token=AI7O7MA32OC47W44IUC6QB245AH7A',
        xref: 'x',
        yref: 'y',
        x: 0,
        y: 40,
        sizex: 40,
        sizey: 40,
        sizing: 'contain',
        opacity: 0.5,
        layer: 'below'
      }],
    width: 1000,
    height: 1000,
    xaxis: {
      range: [0, 40],
      dtick: 1
    },
    yaxis: {
      range: [0, 40],
      dtick: 1
    }
  };

  Plotly.newPlot('plotly-div', [trace1], layout);
}

//helping methods

//convert UTC time to local date time format
function convertUTCDateToLocalDate(date) {
  var newDate = new Date(date);
  newDate.setMinutes(date.getMinutes() - date.getTimezoneOffset());
  return newDate;
}

function temperatureChart() {
  $.getJSON('/api/getenv', function (data) {

    //to get unique sensor_mac
    var sensor_macs = [];
    $.each(data, function (key, val) {
      if (sensor_macs.indexOf(val['sensor_mac']) == -1) {
        sensor_macs.push(val['sensor_mac']);
      }
    });

    //chart displays diff lines for diff sensor_mac
    var dataset = [];

    //colors for multiple sensors
    var colors = ['#4B89DA', '#E8563F', 'D670AC'];
    var count = 0;

    $.each(sensor_macs, function (e) {
      //looping through each indiv sensor_mac
      var tempHeatmap_x = [];
      var tempHeatmap_y = [];
      var tempHeatmap_counter = 0;
      var name = "";

      $.each(data, function (key, val) {
        if (sensor_macs[e] == val['sensor_mac']) {
          tempHeatmap_x[tempHeatmap_counter] = new Date(parseInt(val['timestamp']) * 1000);
          tempHeatmap_y[tempHeatmap_counter] = val['temp'];
          tempHeatmap_counter = tempHeatmap_counter + 1;
          name = val['name'];
        }
      });
      var trace1 = {
        x: tempHeatmap_x,
        y: tempHeatmap_y,
        mode: 'lines',
        name: name,
        marker: { color: colors[count] }
      };
      dataset.push(trace1); //add to dataset
      count = count + 1;
    });

    var layout = {
      title: 'Escalators\' temperature',
      font: { size: 12 },
      xaxis: { title: 'Time of Day', titlefont: { size: 12, color: '#646C77' } },
      yaxis: { title: 'Temperature', titlefont: { size: 12, color: '#646C77' } }
    };

    Plotly.newPlot('temperatureHeatmap', dataset, layout, { responsive: true });
  });
}

function lightChart() {
  $.getJSON('/api/getenv', function (data) {

    //to get unique sensor_mac
    var sensor_macs = [];
    $.each(data, function (key, val) {
      if (sensor_macs.indexOf(val['sensor_mac']) == -1) {
        sensor_macs.push(val['sensor_mac']);
      }
    });

    //chart displays diff lines for diff sensor_mac
    var dataset = [];

    //colors for multiple sensors
    var colors = ['#4B89DA', '#E8563F', 'D670AC'];
    var count = 0;

    $.each(sensor_macs, function (e) {
      //looping through each indiv sensor_mac
      var lightHeatmap_x = [];
      var lightHeatmap_y = [];
      var lightHeatmap_counter = 0;
      var name = "";

      $.each(data, function (key, val) {
        if (sensor_macs[e] == val['sensor_mac']) {
          lightHeatmap_x[lightHeatmap_counter] = new Date(parseInt(val['timestamp']) * 1000);
          lightHeatmap_y[lightHeatmap_counter] = val['light'];
          lightHeatmap_counter = lightHeatmap_counter + 1;
          name = val['name'];
        }
      });
      var trace1 = {
        x: lightHeatmap_x,
        y: lightHeatmap_y,
        mode: 'lines',
        name: name,
        marker: { color: colors[count] }
      };
      dataset.push(trace1); //add to dataset
      count = count + 1;
    });

    var layout = {
      title: 'Escalators\' light',
      font: { size: 12 },
      xaxis: { title: 'Time of Day', titlefont: { size: 12, color: '#646C77' } },
      yaxis: { title: 'Light Level', titlefont: { size: 12, color: '#646C77' } }
    };

    Plotly.newPlot('lightHeatmap', dataset, layout, { responsive: true });
  });
}

function meanChart() {
  $.getJSON('/api/generatemeangraph?sensor_mac=c96056cb3bc1', function (data) {
    var trace1 = {
      x: data["x"],
      y: data["mean"],
      type: 'scatter',
      name: 'mean',
      marker: { color: 'rgb(75,137,218)' }
    };

    var trace2 = {
      x: data["x"],
      y: data["y"][0],
      type: 'line',
      name: 'gradient of mean',
      marker: { color: 'rgb(232,86,63)' }
    };

    var data = [trace1, trace2];

    var layout = {
      title: 'Mean of Vibration for Sensor step-1',
      font: { size: 16 },
      xaxis: { title: 'Time of Day', titlefont: { size: 16, color: '#646C77' } },
      yaxis: { title: 'Vibration Levels', titlefont: { size: 16, color: '#646C77' } }
    };
    Plotly.newPlot('meanTrendline', data, layout, { responsive: true });

  });
}

function barChart() {
  $.getJSON('/api/generatebargraph?sensor_mac=c96056cb3bc1', function (data) {
    var trace1 = {
      x: data["neg_x"],
      y: data["neg_y"],
      type: 'bar',
      name: 'negative',
      marker: { color: 'rgb(217,68,82)' }
    };

    var trace2 = {
      x: data["pos_x"],
      y: data["pos_y"],
      type: 'bar',
      name: 'positive',
      marker: { color: 'rgb(180,224,128)' }
    };

    var data = [trace1, trace2];
    var layout = {
      title: 'Difference in Mean of Vibration for Sensor step-1',
      font: { size: 16 },
      xaxis: { title: 'Time of Day', titlefont: { size: 16, color: '#646C77' } },
      yaxis: { title: 'Difference in Vibration Levels', titlefont: { size: 16, color: '#646C77' } }
    };
    Plotly.newPlot('barGraph', data, layout, { responsive: true });

  });
}

function displayButtons() {
  $.getJSON('/api/getdistinctsensors', function (data) { //display number of buttons dynamically
    var sensorButtons = $("#sensor-btns");

    $.each(data, function (key, val) {
      sensorButtons.append('<button type="button" class="btn btn-info btn-lg" onClick = "toggleChart(\'' + val['sensor_mac'] + '\',\'' + val['sensor_name'] + '\')">' + val['sensor_name'] + '</button>&nbsp;');
    });
  });
}

function toggleChart(input, sensorname) {

  $.getJSON('/api/generatemeangraph?sensor_mac=' + input, function (data) {
    var trace1 = {
      x: data["x"],
      y: data["mean"],
      type: 'scatter',
      name: 'mean',
      marker: { color: 'rgb(75,137,218)' }
    };

    var trace2 = {
      x: data["x"],
      y: data["y"][0],
      type: 'line',
      name: 'gradient of mean',
      marker: { color: 'rgb(232,86,63)' }
    };

    var data = [trace1, trace2];

    var layout = {
      title: 'Mean of Vibration for Sensor ' + sensorname,
      font: { size: 18 },
      xaxis: { title: 'Time of Day', titlefont: { size: 16, color: '#646C77' } },
      yaxis: { title: 'Vibration Levels', titlefont: { size: 16, color: '#646C77' } }
    };
    Plotly.newPlot('meanTrendline', data, layout, { responsive: true });

  });


  $.getJSON('/api/generatebargraph?sensor_mac=' + input, function (data) {
    var trace1 = {
      x: data["neg_x"],
      y: data["neg_y"],
      type: 'bar',
      name: 'negative',
      marker: { color: 'rgb(217,68,82)' }
    };

    var trace2 = {
      x: data["pos_x"],
      y: data["pos_y"],
      type: 'bar',
      name: 'positive',
      marker: { color: 'rgb(180,224,128)' }
    };

    var data = [trace1, trace2];
    var layout = {
      title: 'Difference in Mean of Vibration for Sensor ' + sensorname,
      font: { size: 18 },
      xaxis: { title: 'Time of Day', titlefont: { size: 16, color: '#646C77' } },
      yaxis: { title: 'Difference in Vibration Levels', titlefont: { size: 16, color: '#646C77' } }
    };
    Plotly.newPlot('barGraph', data, layout, { responsive: true });

  });
}

function getAlertStatus() {

  $.getJSON('/api/alertstatus', function (data) {

    $('#dots-l1 span.up').removeClass('dotgreen').removeClass('dotred').addClass('dotgreen');
    $('.dots-status span').removeClass('dotgreen').removeClass('dotred').addClass('dotgreen');
    $('.dots-status span i').removeClass('fa-check').removeClass('fa-times').addClass('fa-check');

    $.each(data, function (key, val) {
      var type = (val['type']);
      $('#dots-' + type + ' span').removeClass('dotgreen').addClass('dotred');
      $('#dots-' + type + ' span i').removeClass('fa-check').addClass('fa-times');

      $('#dots-l1 span.up').removeClass('dotgreen').addClass('dotred');
    });
  });
}

function alertGraph() {

  var layout = {
    barmode: "group",
    title: "Alerts Status",
    font: { size: 14 },
    xaxis: { title: 'Alert types', titlefont: { size: 16, color: '#646C77' } },
    yaxis: { title: 'Frequency', titlefont: { size: 16, color: '#646C77' } }
  };

  $.getJSON('/api/getalertcount', function (data) {

    var colors = ['#fa6c51', '#fc8370', '#f5ba45', '#fcd277', '#46ceac', '#62ddbd', '#4a88da', '#73b1f4'];
    var x1 = [];
    var y1 = [];
    var x2 = [];
    var y2 = [];

    $.each(data, function (key, val) {
      x1.push(val['type']);
      y1.push(val['total'] - val['unresolved']);
      x2.push(val['type']);
      y2.push(val['total']);
    });

    var trace1 = {
      x: x1,
      y: y1,
      type: 'bar',
      text: y1,
      textposition: 'auto',
      hoverinfo: 'none',
      opacity: 0.5,
      name: 'unresolved',
      marker: {
        color: '#62ddbd',
        line: {
          color: '#46ceac',
          width: 1.5
        }
      }
    };

    var trace2 = {
      x: x2,
      y: y2,
      type: 'bar',
      text: y2,
      textposition: 'auto',
      hoverinfo: 'none',
      name: 'total',
      marker: {
        color: '#73b1f4',
        line: {
          color: '#4a88da',
          width: 1.5
        }
      }
    };
    var d = [trace1, trace2];

    Plotly.newPlot('alertGraph', d, layout, { responsive: true });
  });

}

function trafficGraph() {
  $.getJSON('/api/getsensorrangeagg', function (data) {
    timestamps = [];
    num_passengers = [];
    average_pass = [];


    $.each(data, function (key, val) {
      num_passengers.push(val['num_passenger']);
      timestamps.push(new Date(parseInt(val['timestamp']) * 1000));
      average_pass.push(val['avg_num_passenger']);
    });

    var trace1 = {
      x: timestamps,
      y: average_pass,
      type: 'scatter',
      name: 'Avg Passengers per Min',
      label: 'Average Num of Passengers per Min'
    };

    var trace2 = {
      x: timestamps,
      y: num_passengers,
      type: 'bar',
      name: 'Num Passengers per Min',
      label: 'Number of Passengers per Min'
    };

    var layout = {
      title: 'Passenger Traffic per Min',
      font: { size: 14 },
      xaxis: { title: 'Time of Day', titlefont: { size: 16, color: '#646C77' } },
      yaxis: { title: 'Num of Passengers', titlefont: { size: 16, color: '#646C77' } }
    }

    var data = [trace1, trace2];


    Plotly.newPlot('trafficCount', data, layout, { responsive: true });
  });
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
$(document).ready(function () {

  getAlertStatus();
  temperatureChart();
  lightChart();
  meanChart();
  barChart();
  displayButtons();
  alertGraph();
  trafficGraph();
  plotFloor();

  setInterval(getAlertStatus, 1000 * 5);
  setInterval(temperatureChart, 1000 * 30);
  setInterval(lightChart, 1000 * 30);
  setInterval(alertGraph, 1000 * 30);
  setInterval(trafficGraph, 1000 * 60);
  setInterval(meanChart, 1000 * 60);
  setInterval(barChart, 1000 * 60);


});
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

