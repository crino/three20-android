package com.three20.ns;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class NSDate extends Date {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6203488883212496944L;

	public static final int NSOrderedAscending = -1;
	public static final int NSOrderedSame = 0;
	public static final int NSOrderedDescending = 1;

	public NSDate() {
		super();
	}

	/**
	 * Represents the specified number of seconds from the current date.
	 */
	public NSDate(double seconds) {
		super((long) new NSDate().getTime()
				+ timeIntervalToMilliseconds(seconds));
	}

	/**
	 * Represents the specified number of seconds from the specified date.
	 */
	public NSDate(double seconds, Date sinceDate) {
		super((long) sinceDate.getTime() + timeIntervalToMilliseconds(seconds));
	}

	/**
	 * Returns the interval between this date and 1 January 2001 GMT.
	 */
	public double timeIntervalSinceReferenceDate() {
		GregorianCalendar referenceDate = new GregorianCalendar(TimeZone
				.getTimeZone("GMT"));
		referenceDate.set(2001, 0, 0, 0, 0, 0);
		return timeIntervalSinceDate(referenceDate.getTime());
	}

	/**
	 * Returns the interval between this date and the specified date in seconds.
	 */
	public double timeIntervalSinceDate(Date aDate) {
		return millisecondsToTimeInterval(this.getTime() - aDate.getTime());
	}

	/**
	 * Returns the interval between this date and the current date in seconds.
	 */
	public double timeIntervalSinceNow() {
		return timeIntervalSinceDate(new NSDate());
	}

	/**
	 * Compares this date to the specified date and returns the earlier date.
	 * Unspecified which is returned if both are equal.
	 */
	public NSDate earlierDate(NSDate aDate) {
		if (aDate == null)
			return this;
		if (after(aDate))
			return aDate;
		return this;
	}

	/**
	 * Compares this date to the specified date and returns the later date.
	 * Unspecified which is returned if both are equal.
	 */
	public NSDate laterDate(NSDate aDate) {
		if (aDate == null)
			return this;
		if (before(aDate))
			return aDate;
		return this;
	}

	/**
	 * Returns a negative value if the specified date is later than this date, a
	 * positive value if the specified date is earlier than this date, or zero
	 * if the dates are equal. The return values are compatible with type
	 * NSComparisonResult.
	 */
	public int compare(Date aDate) {
		if (before(aDate))
			return NSOrderedAscending;
		if (after(aDate))
			return NSOrderedDescending;
		return NSOrderedSame;
	}

	/**
	 * Returns whether the this date is equal to the specified date, per the
	 * result of equals().
	 */
	public boolean isEqualToDate(Date aDate) {
		return equals(aDate);
	}

	/**
	 * Returns the number of seconds between now and the reference date.
	 */
	public static double currentTimeIntervalSinceReferenceDate() {
		return new NSDate().timeIntervalSinceReferenceDate();
	}

	/**
	 * Returns a date that differs from this date by the specified number of
	 * seconds.
	 */
	public NSDate dateByAddingTimeInterval(double seconds) {
		return new NSDate(seconds, this);
	}

	/**
	 * Converts seconds to milliseconds. Included for compatibility.
	 */
	public static long timeIntervalToMilliseconds(double seconds) {
		return (long) seconds * 1000;
	}

	/**
	 * Converts milliseconds to seconds. Included for compatibility.
	 */
	public static double millisecondsToTimeInterval(long millis) {
		return millis / 1000.0;
	}
}
