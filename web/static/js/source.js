

function plotFloor() {
 
  $.getJSON('/api/crowd_report',function(data){
  console.log(data)
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



  // var socket = io.connect('http://' + '13.250.107.52' + ':' + '8080');

  // socket.on('updates', function (data) {
  //   console.log(data)
  var trace1 = {
    x: data['x'],
    y: data['y'],
    mode: 'markers',
    marker: {
      size: data['count']
    },
    type: 'scatter'
    
  };

    Plotly.plot('plotly-div', [trace1], layout);
  })


};


/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
$(document).ready(function () {


  plotFloor();
  

});
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

