/*
 * (c) Copyright Christian P. Fries, Germany. All rights reserved. Contact: email@christianfries.com.
 *
 * Created on 21 May 2018
 */

package net.finmath.plots.experimental;

import java.util.function.DoubleBinaryOperator;

import org.jfree.chart.plot.Plot;
import org.jzy3d.analysis.AbstractAnalysis;
import org.jzy3d.analysis.AnalysisLauncher;
import org.jzy3d.chart.factories.AWTChartComponentFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.colormaps.ColorMapRainbow;
import org.jzy3d.maths.Range;
import org.jzy3d.plot3d.builder.Builder;
import org.jzy3d.plot3d.builder.Mapper;
import org.jzy3d.plot3d.builder.concrete.OrthonormalGrid;
import org.jzy3d.plot3d.primitives.AbstractDrawable;
import org.jzy3d.plot3d.primitives.Shape;
import org.jzy3d.plot3d.primitives.axes.AxeBox;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import org.jzy3d.plot3d.text.ITextRenderer;
import org.jzy3d.plot3d.text.renderers.TextBillboardRenderer;
import org.jzy3d.plot3d.text.renderers.TextBitmapRenderer;
import org.jzy3d.plot3d.text.renderers.jogl.JOGLTextRenderer;
import org.jzy3d.plot3d.text.renderers.jogl.ShadowedTextStyle;

/**
 * Small convenient wrapper for JZY3D derived from the JZY3D SurfaceDemo.
 * 
 * @author Christian Fries
 */
public class Plot3D {

	private double xmin, xmax;
	private double ymin, ymax;
	private int numberOfPointsX, numberOfPointsY;
	private DoubleBinaryOperator function;
	private String labelX = "x";
	private String labelY = "y";
	private String labelZ = "z";

	public Plot3D(double xmin, double xmax, double ymin, double ymax, int numberOfPointsX, int numberOfPointsY, DoubleBinaryOperator function) {
		super();
		this.xmin = xmin;
		this.xmax = xmax;
		this.ymin = ymin;
		this.ymax = ymax;
		this.numberOfPointsX = numberOfPointsX;
		this.numberOfPointsY = numberOfPointsY;
		this.function = function;
	}

	class Surface extends AbstractAnalysis {

	    public void init() {
	        // Define a function to plot
	        Mapper mapper = new Mapper() {
	            @Override
	            public double f(double x, double y) {
	                return Plot3D.this.function.applyAsDouble(x,y);
	            }
	        };

	        // Create the object to represent the function over the given range.
	        final Shape surface = Builder.buildOrthonormal(new OrthonormalGrid(new Range((float)xmin,(float)xmax), numberOfPointsX, new Range((float)ymin,(float)ymax), numberOfPointsY), mapper);
	        surface.setColorMapper(new ColorMapper(new ColorMapRainbow(), surface.getBounds().getZmin(), surface.getBounds().getZmax(), new Color(1, 1, 1, .5f)));
	        surface.setFaceDisplayed(true);
	        surface.setWireframeDisplayed(false);

	        // Create a chart
	        chart = AWTChartComponentFactory.chart(Quality.Advanced, getCanvasType());
	        chart.getScene().getGraph().add(surface);

			chart.getAxeLayout().setXAxeLabelDisplayed(true);
			chart.getAxeLayout().setYAxeLabelDisplayed(true);
//			chart.getAxeLayout().setXTickLabelDisplayed(true);
			chart.getAxeLayout().setXAxeLabel(labelX);
			chart.getAxeLayout().setYAxeLabel(labelY);
			chart.getAxeLayout().setZAxeLabel(labelZ);
//			chart.getAxeLayout().setYTickRenderer( new DateTickRenderer( "dd/MM/yyyy" ) );
//			chart.getAxeLayout().setZAxeLabel( "Z" );
			//chart.getAxeLayout().setZTickRenderer( new ScientificNotationTickRenderer(2) );


			AxeBox box = (AxeBox)chart.getView().getAxe();
//			ITextRenderer renderer2 = new JOGLTextRenderer(new ShadowedTextStyle(128f, 10, java.awt.Color.RED, java.awt.Color.CYAN));
//			ITextRenderer renderer3 = new TextBillboardRenderer();
			ITextRenderer renderer = new TextBitmapRenderer(TextBitmapRenderer.Font.TimesRoman_24);
			box.setTextRenderer(renderer);
	    }
	}

	public void show() throws Exception {
		AnalysisLauncher.open(this.new Surface());
	}

	public Plot3D setLabelX(String labelX) {
		this.labelX = labelX;
		return this;
	}

	public Plot3D setLabelY(String labelY) {
		this.labelY = labelY;
		return this;
	}

	public Plot3D setLabelZ(String labelZ) {
		this.labelZ = labelZ;
		return this;
	}
}
