/*
 * (c) Copyright Christian P. Fries, Germany. Contact: email@christianfries.com.
 *
 * Created on 21 May 2018
 */

package net.finmath.plots;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.Collectors;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import net.finmath.plots.jfreechart.JFreeChartUtilities;

/**
 * Small convenient wrapper for JFreeChart line plot derived.
 *
 * @author Christian Fries
 */
public class Plot2D implements Plot {

	private List<Plotable2D> plotables;

	private String title = "";
	private String xAxisLabel = "x";
	private String yAxisLabel = "y";
	private NumberFormat xAxisNumberFormat;
	private NumberFormat yAxisNumberFormat;
	private Boolean isLegendVisible = false;

	private transient JFreeChart chart;
	private Object updateLock = new Object();

	private Double ymin;
	private Double ymax;

	public Plot2D(double xmin, double xmax, int numberOfPointsX, DoubleUnaryOperator function) {
		this(xmin, xmax, numberOfPointsX, Collections.singletonList(new Named<DoubleUnaryOperator>("",function)));
	}

	public Plot2D(double xmin, double xmax, int numberOfPointsX, List<Named<DoubleUnaryOperator>> doubleUnaryOperators) {
		this(doubleUnaryOperators.stream().map(namedFunction -> { return new PlotableFunction2D(xmin, xmax, numberOfPointsX, namedFunction, null); }).collect(Collectors.toList()));
	}

	public Plot2D(List<Plotable2D> plotables) {
		super();
		this.plotables = plotables;
	}

	private void init() {
		synchronized (updateLock) {
			if(chart != null) return;

			XYLineAndShapeRenderer renderer	= new XYLineAndShapeRenderer();
			XYSeriesCollection data = new XYSeriesCollection();
			chart = JFreeChartUtilities.getXYPlotChart(title, xAxisLabel, "#.##" /* xAxisNumberFormat */, yAxisLabel, "#.##" /* yAxisNumberFormat */, data, renderer, isLegendVisible);
		}
	}

	private void update() {
		XYLineAndShapeRenderer renderer	= new XYLineAndShapeRenderer();
		XYSeriesCollection data = new XYSeriesCollection();
		for(int functionIndex=0; functionIndex<plotables.size(); functionIndex++) {
			Plotable2D plotable = plotables.get(functionIndex);

			List<Point2D> plotableSeries = plotable.getSeries();
			XYSeries series = new XYSeries(plotable.getName());
			for(int i = 0; i<plotableSeries.size(); i++) {
				series.add(plotableSeries.get(i).getX(), plotableSeries.get(i).getY());
			}
			data.addSeries(series);

			GraphStyle style = plotable.getStyle();
			Color color = style != null ? plotable.getStyle().getColor() : null;
			if(color == null) color = getDefaultColor(functionIndex);
			renderer.setSeriesPaint(functionIndex, color);

			if(style != null) {
				renderer.setSeriesShape(functionIndex, plotable.getStyle().getShape());
				renderer.setSeriesStroke(functionIndex, plotable.getStyle().getStoke());
				renderer.setSeriesShapesVisible(functionIndex, style.getShape() != null);
				renderer.setSeriesLinesVisible(functionIndex, style.getStoke() != null);
			}
		}

		synchronized (updateLock) {
			if(chart != null) {
				chart.getXYPlot().setDataset(0, data);
				chart.getXYPlot().setRenderer(0, renderer);

				NumberAxis domain = (NumberAxis) chart.getXYPlot().getDomainAxis();
				if(xAxisNumberFormat != null) domain.setNumberFormatOverride(xAxisNumberFormat);

				NumberAxis range = (NumberAxis) chart.getXYPlot().getRangeAxis();
				if(yAxisNumberFormat != null) range.setNumberFormatOverride(yAxisNumberFormat);
				if(ymin != null && ymin != null) {
					range.setAutoRange(false);
					range.setRange(new Range(ymin, ymax));
				}
				else {
					range.setAutoRange(true);
				}
			}
		}
	}

	private Color getDefaultColor(int functionIndex) {
		switch (functionIndex) {
		case 0:
			return new java.awt.Color(255, 0,  0);
		case 1:
			return new java.awt.Color(0, 255,  0);
		case 2:
			return new java.awt.Color(0, 0,  255);
		default:
			return new java.awt.Color(0, 0,  0);
		}
	}

	@Override
	public void show() {
		init();
		update(plotables);
		JPanel chartPanel = new ChartPanel(chart,
				800, 400,   // size
				128, 128,   // minimum size
				2024, 2024, // maximum size
				false, true, true, false, true, false);    // useBuffer, properties, save, print, zoom, tooltips

		java.awt.EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				synchronized (updateLock) {
					JFrame frame = new JFrame();
					frame.add(chartPanel);
					frame.setVisible(true);
					frame.pack();
				}
			}
		});
	}

	@Override
	public void saveAsJPG(File file, int width, int height) throws IOException {
		init();
		update(plotables);
		synchronized (updateLock) {
			JFreeChartUtilities.saveChartAsJPG(file, chart, width, height);
		}
	}

	@Override
	public void saveAsPDF(File file, int width, int height) throws IOException {
		init();
		update(plotables);
		synchronized (updateLock) {
			JFreeChartUtilities.saveChartAsPDF(file, chart, width, height);
		}
	}

	@Override
	public void saveAsSVG(File file, int width, int height) throws IOException {
		init();
		update(plotables);
		synchronized (updateLock) {
			JFreeChartUtilities.saveChartAsSVG(file, chart, width, height);
		}
	}

	public Plot2D update(List<Plotable2D> plotables) {
		this.plotables = plotables;
		synchronized (updateLock) {
			if(chart != null) {
				update();
			}
		}
		return this;
	}

	@Override
	public Plot2D setTitle(String title) {
		this.title = title;
		update();
		return this;
	}

	@Override
	public Plot2D setXAxisLabel(String xAxisLabel) {
		this.xAxisLabel = xAxisLabel;
		synchronized (updateLock) {
			if(chart != null) {
				chart.getXYPlot().getDomainAxis().setLabel(xAxisLabel);
			}
		}
		return this;
	}

	@Override
	public Plot2D setYAxisLabel(String yAxisLabel) {
		this.yAxisLabel = yAxisLabel;
		synchronized (updateLock) {
			if(chart != null) {
				chart.getXYPlot().getRangeAxis().setLabel(yAxisLabel);
			}
		}
		return this;
	}

	@Override
	public Plot setZAxisLabel(String zAxisLabel) {
		throw new UnsupportedOperationException("The 2D plot does not suport a z-axis. Try 3D plot instead.");
	}

	public Plot2D setxAxisNumberFormat(NumberFormat xAxisNumberFormat) {
		this.xAxisNumberFormat = xAxisNumberFormat;
		return this;
	}

	public Plot2D setyAxisNumberFormat(NumberFormat yAxisNumberFormat) {
		this.yAxisNumberFormat = yAxisNumberFormat;
		return this;
	}

	public Plot setYRange(double ymin, double ymax) {
		this.ymin = ymin;
		this.ymax = ymax;

		update();

		return this;
	}

	/**
	 * @param isLegendVisible the isLegendVisible to set
	 */
	public Plot setIsLegendVisible(Boolean isLegendVisible) {
		this.isLegendVisible = isLegendVisible;
		update();
		return this;
	}
}
