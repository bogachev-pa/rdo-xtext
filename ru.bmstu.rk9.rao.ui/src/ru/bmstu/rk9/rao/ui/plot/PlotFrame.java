package ru.bmstu.rk9.rao.ui.plot;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.events.DragDetectEvent;
import org.eclipse.swt.events.DragDetectListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Slider;
import org.jfree.chart.annotations.XYShapeAnnotation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.data.xy.XYDataset;
import org.jfree.experimental.chart.swt.ChartComposite;

public class PlotFrame extends ChartComposite {

	private Slider horizontalSlider;
	private Slider verticalSlider;
	private double horizontalMaximum;
	private double verticalMaximum;
	private double horizontalRatio;
	private double verticalRatio;

	@Override
	public void setDomainZoomable(boolean zoomable) {
		super.setDomainZoomable(zoomable);
		if (horizontalSlider != null) {
			updateSliders();
		}
	}

	@Override
	public void setRangeZoomable(boolean zoomable) {
		super.setRangeZoomable(zoomable);
		if (verticalSlider != null) {
			updateSliders();
		}
	}

	Point2D swtToPlot(int x, int y) {
		Rectangle rectangle = PlotFrame.this.getScreenDataArea();
		final ValueAxis domainAxis = getChart().getXYPlot().getDomainAxis();
		final ValueAxis rangeAxis = getChart().getXYPlot().getRangeAxis();
		final double width_in_axis_units = domainAxis.getUpperBound() - domainAxis.getLowerBound();
		final double height_in_axis_units = rangeAxis.getUpperBound() - rangeAxis.getLowerBound();
		double relativeX = ((double) x - rectangle.x) / (rectangle.width);
		double relativeY = ((double) y - rectangle.y) / (rectangle.height);
		double x_plot = domainAxis.getLowerBound() + relativeX * width_in_axis_units;
		double y_plot = rangeAxis.getUpperBound() - relativeY * height_in_axis_units;

		return new Point2D.Double(x_plot, y_plot);
	}

	Point plotToSwt(double x, double y) {
		Rectangle rectangle = PlotFrame.this.getScreenDataArea();
		final ValueAxis domainAxis = getChart().getXYPlot().getDomainAxis();
		final ValueAxis rangeAxis = getChart().getXYPlot().getRangeAxis();
		double width_in_axis_units = domainAxis.getUpperBound() - domainAxis.getLowerBound();
		double height_in_axis_units = rangeAxis.getUpperBound() - rangeAxis.getLowerBound();
		double relativeX = (x - domainAxis.getLowerBound()) / width_in_axis_units;
		double relativeY = (rangeAxis.getUpperBound() - y) / height_in_axis_units;
		double screenX = relativeX * rectangle.width + rectangle.x;
		double screenY = relativeY * rectangle.height + rectangle.y;

		return new Point((int) Math.round(screenX), (int) Math.round(screenY));
	}

	final DefaultToolTip toolTip;

	class PlotMouseMoveListener implements MouseMoveListener {

		int previousIndex = -1;

		@Override
		public void mouseMove(MouseEvent e) {
			System.out.println("x: " + e.x + " y:" + e.y);

			XYDataset dataset = getChart().getXYPlot().getDataset();

			int itemsCount = dataset.getItemCount(0);
			if (itemsCount > 0) {
				Point2D plotCoords = swtToPlot(e.x, e.y);
				Point2D swtCoords = new Point2D.Double(e.x, e.y);

				double previousPlotDistance = Double.MAX_VALUE;
				int currentIndex = -1;
				for (int index = 0; index < itemsCount; index++) {
					double valueX = dataset.getXValue(0, index);
					double valueY = dataset.getYValue(0, index);
					double Plotdistance = plotCoords.distance(valueX, valueY);
					Point swtCoordsNext = plotToSwt(valueX, valueY);
					double Swtdistance = swtCoords.distance(swtCoordsNext.x, swtCoordsNext.y);
					if (Plotdistance != previousPlotDistance && Swtdistance < 40) {
						previousPlotDistance = Plotdistance;
						currentIndex = index;
					}

				}

				if (currentIndex >= 0 && (currentIndex != previousIndex)) {
					getChart().getXYPlot().clearAnnotations();
					previousIndex = currentIndex;
					Rectangle rectangle = PlotFrame.this.getScreenDataArea();
					final ValueAxis domainAxis = getChart().getXYPlot().getDomainAxis();
					final ValueAxis rangeAxis = getChart().getXYPlot().getRangeAxis();
					double width_in_axis_units = domainAxis.getUpperBound() - domainAxis.getLowerBound();
					double height_in_axis_units = rangeAxis.getUpperBound() - rangeAxis.getLowerBound();
					double valueX = dataset.getXValue(0, currentIndex);
					double valueY = dataset.getYValue(0, currentIndex);
					Point widgetPoint = plotToSwt(valueX, valueY);
					double radiusOfCircleX = 5 * width_in_axis_units / rectangle.width;
					double radiusOfCircleY = 5 * height_in_axis_units / rectangle.height;
					Ellipse2D Circle = new Ellipse2D.Float((float) (valueX - radiusOfCircleX),
							(float) (valueY - radiusOfCircleY), (float) (2 * radiusOfCircleX),
							(float) (2 * radiusOfCircleY));

					/*
					 * Ellipse CirclE = new Ellipse();
					 * org.eclipse.draw2d.geometry.Point widjetPointforCircle =
					 * plotToSwt(valueX-(int)radiusOfCircleX,
					 * valueY-(int)radiusOfCircleY);
					 * CirclE.setSize(2*(int)radiusOfCircleX,2*(int)
					 * radiusOfCircleY) ;
					 * CirclE.setLocation(widjetPointforCircle);
					 *
					 * abstract class Circle extends Graphics2D {
					 *
					 * @Override public void fill(Shape s) {
					 *
					 * }
					 *
					 * }
					 */
					XYShapeAnnotation annotation = new XYShapeAnnotation(Circle);
					getChart().getXYPlot().addAnnotation(annotation, false);

					Rectangle screenDataArea = getScreenDataArea();
					if (new Rectangle(screenDataArea.x - 1, screenDataArea.y - 1, screenDataArea.width + 2,
							screenDataArea.height + 2).contains(widgetPoint)) {
						toolTip.setText(String.format("x: %.3f y: %.3f", valueX, valueY));
						toolTip.show(widgetPoint);

					} else {
						toolTip.hide();
						getChart().getXYPlot().clearAnnotations();
					}

				}

			}

		}
	}

	class DragDropOperation implements DragDetectListener {

		@Override
		public void dragDetected(DragDetectEvent e) {
			// TODO Auto-generated method stub

		}

	}

	class DragDrop implements DragSourceListener {

		@Override
		public void dragStart(DragSourceEvent event) {
			// TODO Auto-generated method stub

		}

		@Override
		public void dragSetData(DragSourceEvent event) {
			// TODO Auto-generated method stub

		}

		@Override
		public void dragFinished(DragSourceEvent event) {
			// TODO Auto-generated method stub
		}

	}

	class PlotKeyListener implements KeyListener {
		@Override
		public void keyPressed(KeyEvent e) {
			switch (e.keyCode) {
			case SWT.SHIFT: {
				setRangeZoomable(true);
				return;
			}
			case SWT.CTRL: {
				setDomainZoomable(true);
				return;
			}
			case SWT.SPACE:
				restoreAutoBounds();
				return;
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
			if (e.keyCode == SWT.SHIFT) {
				setRangeZoomable(false);
			} else if (e.keyCode == SWT.CTRL) {
				setDomainZoomable(false);
			}
		}
	}

	class PlotMouseWheelListener implements MouseWheelListener {

		@Override
		public void mouseScrolled(MouseEvent e) {
			System.out.println("" + getScaleY());
			System.out.println("" + getScaleX());

			if (e.count > 0 && isRangeZoomable() && getScaleY() >= 0.5) {
				zoomInRange(e.x, e.y);
			}
			if (e.count < 0 && isRangeZoomable() && getScaleY() <= 5) {
				zoomOutRange(e.x, e.y);
			}
			if (e.count > 0 && isDomainZoomable() && getScaleX() >= 0.5) {
				zoomInDomain(e.x, e.y);
			}
			if (e.count < 0 && isDomainZoomable() && getScaleX() <= 5) {
				zoomOutDomain(e.x, e.y);
			}
			if (e.count > 0 && isDomainZoomable() && isRangeZoomable()) {
				zoomInBoth(e.x, e.y);
			}
			if (e.count < 0 && isDomainZoomable() && isRangeZoomable()) {
				zoomOutBoth(e.x, e.y);
			}
			updateSliders();
		}
	}

	public PlotFrame(final Composite comp, final int style) {
		super(comp, style, null, ChartComposite.DEFAULT_WIDTH, ChartComposite.DEFAULT_HEIGHT, 0, 0, Integer.MAX_VALUE,
				Integer.MAX_VALUE, ChartComposite.DEFAULT_BUFFER_USED, true, true, true, true, true);
		addSWTListener(new PlotKeyListener());
		addMouseWheelListener(new PlotMouseWheelListener());
		addSWTListener(new PlotMouseMoveListener());
		// addDragDetectListener(new DragDrop());
		setZoomInFactor(0.75);
		setZoomOutFactor(1.25);
		setDomainZoomable(false);
		setRangeZoomable(false);
		toolTip = new DefaultToolTip(this, SWT.BALLOON, false);

		toolTip.setBackgroundColor(new Color(comp.getDisplay(), 255, 255, 255));
		toolTip.setForegroundColor(new Color(comp.getDisplay(), 128, 128, 128));
		toolTip.setRespectDisplayBounds(true);
		toolTip.setShift(new Point(0, -50));

	}

	public final void setSliders(final Slider horizontalSlider, final Slider verticalSlider) {
		this.horizontalSlider = horizontalSlider;
		this.verticalSlider = verticalSlider;
		horizontalSlider.setMinimum(0);
		verticalSlider.setMinimum(0);
		this.horizontalSlider.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				getChart().getXYPlot().getDomainAxis().setLowerBound(horizontalSlider.getSelection() / horizontalRatio);
				getChart().getXYPlot().getDomainAxis().setUpperBound(
						(horizontalSlider.getThumb() + horizontalSlider.getSelection()) / horizontalRatio);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		this.verticalSlider.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				getChart().getXYPlot().getRangeAxis().setLowerBound(
						(verticalSlider.getMaximum() - verticalSlider.getSelection() - verticalSlider.getThumb())
								/ verticalRatio);
				getChart().getXYPlot().getRangeAxis()
						.setUpperBound((verticalSlider.getMaximum() - verticalSlider.getSelection()) / verticalRatio);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	public final void setChartMaximum(final double horizontalMaximum, final double verticalMaximum) {
		final double freeSpaceCoefficient = 1.05;
		this.horizontalMaximum = horizontalMaximum * freeSpaceCoefficient;
		this.verticalMaximum = verticalMaximum * freeSpaceCoefficient;
	}

	public final void updateSliders() {
		final ValueAxis domainAxis = getChart().getXYPlot().getDomainAxis();
		final ValueAxis rangeAxis = getChart().getXYPlot().getRangeAxis();

		if (horizontalMaximum - domainAxis.getRange().getUpperBound() > 0.001) {
			horizontalSlider.setVisible(true);
			horizontalSlider.setEnabled(true);
			horizontalRatio = horizontalSlider.getMaximum() / horizontalMaximum;
			horizontalSlider.setThumb(
					(int) Math.round((domainAxis.getUpperBound() - domainAxis.getLowerBound()) * horizontalRatio));
			horizontalSlider.setSelection((int) Math.round(domainAxis.getLowerBound() * horizontalRatio));
		} else {
			horizontalSlider.setVisible(false);
			horizontalSlider.setEnabled(false);

		}
		if (verticalMaximum - rangeAxis.getRange().getUpperBound() > 0.001) {
			verticalSlider.setVisible(true);
			verticalSlider.setEnabled(true);

			verticalRatio = verticalSlider.getMaximum() / verticalMaximum;
			verticalSlider.setThumb(
					(int) Math.round((rangeAxis.getUpperBound() - rangeAxis.getLowerBound()) * verticalRatio));
			verticalSlider
					.setSelection((int) Math.round((verticalMaximum - rangeAxis.getUpperBound()) * verticalRatio));
		} else {
			verticalSlider.setVisible(false);
			verticalSlider.setEnabled(false);

		}
	}

	@Override
	public void zoom(Rectangle selection) {
		super.zoom(selection);
		horizontalSlider.setVisible(true);
		horizontalSlider.setEnabled(true);
		if (this.isRangeZoomable()) {
			verticalSlider.setVisible(true);
			verticalSlider.setEnabled(true);
		}
		updateSliders();
	}

	@Override
	public void restoreAutoBounds() {
		super.restoreAutoBounds();
		horizontalSlider.setEnabled(false);
		horizontalSlider.setVisible(false);
		verticalSlider.setEnabled(false);
		verticalSlider.setVisible(false);
	}

}
