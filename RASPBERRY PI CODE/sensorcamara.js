var statistics = require('math-statistics');
var usonic = require ('r-pi-usonic');

var init = function(config)
{
	var sensor = usonic.sensor(config.echoPin, config.triggerPin, config.timeout);
	var distances;
	
	(function measure()
	{
		if(!distances || distances.length === config.rate)
		{
			if(distances)
			{
				print(distances);
				}
			distances = [];
			}
		setTimeout(function()
		{
			distances.push(sensor());
			
			measure();
			
			}, config.delay);
		}());
	};
	
var print = function(distances)
{
	var distance = statistics.median(distances);
	
	process.stdout.clearLine();
	process.stdout.cursorTo(0);
	
	if(distance < 0)
	{
		process.stdout.write('Error: Measurement timeout.\n');
		}
	else
	{
		process.stdout.write('Distance: '+ distance.toFixed(2) + ' cm');
		}
	};

init(
{
	echoPin:18,
	triggerPin:16,
	timeout: 1000,
	delay: 60,
	rate: 5
	});
